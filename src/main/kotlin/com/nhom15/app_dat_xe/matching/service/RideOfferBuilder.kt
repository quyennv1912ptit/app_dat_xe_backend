package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.account.repository.UsersRepository
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.common.util.GeoUtils.distanceKm
import com.nhom15.app_dat_xe.matching.dto.CustomerBrief
import com.nhom15.app_dat_xe.matching.dto.RideOfferPayload
import com.nhom15.app_dat_xe.matching.dto.VehicleInfo
import com.nhom15.app_dat_xe.ride.repository.RidesRepository
import com.nhom15.app_dat_xe.tracking.service.MapService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import kotlin.math.ceil
import kotlin.math.roundToLong

@Component
class RideOfferBuilder(
    private val ridesRepository: RidesRepository,
    private val mapService: MapService,
    private val redisTemplate: StringRedisTemplate,
    private val usersRepository: UsersRepository
) {
    /**
     * Phải gọi TRƯỚC khi rút tài xế khỏi GEO (removeFromPool), vì vị trí được đọc từ available_drivers.
     * [expiresAt] là mốc epoch ms đã dùng để ghi vào dispatch:timeouts, truyền vào để hai nơi không lệch nhau.
     */
    fun build(rideId: Long, driverId: Long, expiresAt: Long): RideOfferPayload {
        val ride = ridesRepository.findByIdOrNull(rideId)
            ?: throw NotFoundException(message = "Không tìm thấy chuyến $rideId")

        val customer = usersRepository.findByIdOrNull(ride.customerId)

        val route = mapService.getDistanceAndDuration(
            origin = "${ride.pickup.geoPoint.lng},${ride.pickup.geoPoint.lat}",
            destination = "${ride.dropoff.geoPoint.lng},${ride.dropoff.geoPoint.lat}"
        )

        val driverPoint = redisTemplate.opsForGeo()
            .position(RedisKeys.GEO_KEY, driverId.toString())
            ?.firstOrNull()
            ?: throw NotFoundException(message = "Không có vị trí của tài xế $driverId")

        val toPickupKm = distanceKm(GeoPoint(driverPoint.y, driverPoint.x), ride.pickup.geoPoint)
        val straightKm = distanceKm(ride.pickup.geoPoint, ride.dropoff.geoPoint)

        // Quãng đường cả chuyến: ưu tiên số đã lưu, rồi đến Mapbox, cuối cùng là đường chim bay nhân hệ số
        val routeKm = route.distance / 1000.0
        val tripKm = ride.distanceKm
            ?: routeKm.takeIf { it > 0 }
            ?: (straightKm * ROAD_FACTOR)
        val tripMin = if (route.duration > 0) ceil(route.duration / 60.0).toInt() else minutesFor(tripKm)

        return RideOfferPayload(
            rideId = ride.id.toString(),
            expiresAt = expiresAt,
            pickup = ride.pickup,
            destination = ride.dropoff,
            distanceToPickupKm = round1(toPickupKm),
            etaToPickupMin = minutesFor(toPickupKm),
            tripDistanceKm = round1(tripKm),
            estDurationMin = tripMin,
            fareEstimate = ride.fareEstimate,
            driverEarning = (ride.fareEstimate * DRIVER_SHARE).roundToLong(),
            paymentMethod = ride.paymentMethod,
            vehicle = VehicleInfo(ride.vehicleType.name, ride.transmission),
            customer = CustomerBrief(
                displayName = customer?.fullName?.substringAfterLast(' ')?.takeIf { it.isNotBlank() } ?: "Khách",
                rating = customer?.averageRating ?: 5.0
            ),
            note = ride.note
        )
    }

    private fun minutesFor(km: Double): Int =
        ceil(km / AVG_SPEED_KMH * 60).toInt().coerceAtLeast(1)

    private fun round1(x: Double): Double = Math.round(x * 10) / 10.0

    private companion object {
        const val AVG_SPEED_KMH = 25.0
        const val ROAD_FACTOR = 1.3
        const val DRIVER_SHARE = 0.8
    }
}