package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.ride.event.RideRequested
import com.nhom15.app_dat_xe.ride.event.RideCompleted
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideCandidates
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideState
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver
import com.nhom15.app_dat_xe.tracking.service.LocationService
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RideDispatchService (
    val locationService: LocationService,
    val selectionService: DriverSelectionService,
    val redisTemplate: StringRedisTemplate
) {
    @EventListener
    fun handleRideRequested(event: RideRequested) {
        val radius = 2.0
        val transmission = Transmission.AUTO
        val nearbyDrivers: List<NearbyDriver> = locationService.findNearbyDrivers(event.pickup, radius)

        val rankedDrivers = selectionService.filterAndRankDrivers(nearbyDrivers, transmission)

        val rankedDriverIds = rankedDrivers.map { it.driverId.toString() }

        if (rankedDrivers.isEmpty()) {

        } else {
            val queueKey = rideCandidates("100")
            redisTemplate.opsForList().rightPushAll(queueKey, rankedDriverIds)
            redisTemplate.expire(queueKey, Duration.ofHours(1))

            val rideStatusMap = mapOf(
                "status" to RideStatus.SEARCHING.name,
                "customerId" to event.customerId.toString(),
                "transmission" to event.transmission.name
            )

            val statusKey = rideState("100")

            redisTemplate.opsForHash<String, String>().putAll(statusKey, rideStatusMap)

            redisTemplate.expire(statusKey, Duration.ofHours(24))
        }
    }

    @EventListener
    fun handleRideCompleted(event: RideCompleted) {
        val rideId = event.rideId
        val statusKey = rideState(rideId.toString())
        redisTemplate.opsForHash<String, String>().put(statusKey, "status", "COMPLETED")
        redisTemplate.expire(statusKey, Duration.ofHours(1))
    }
}