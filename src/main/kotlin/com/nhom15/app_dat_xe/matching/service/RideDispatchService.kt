package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideCandidates
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideState
import com.nhom15.app_dat_xe.notification.service.NotificationService
import com.nhom15.app_dat_xe.ride.event.RideCompleted
import com.nhom15.app_dat_xe.ride.event.RideNoDriverFound
import com.nhom15.app_dat_xe.ride.event.RideRequested
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver
import com.nhom15.app_dat_xe.tracking.service.LocationService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.Duration

@Service
class RideDispatchService(
    val locationService: LocationService,
    val selectionService: DriverSelectionService,
    val redisTemplate: StringRedisTemplate,
    val notificationService: NotificationService,
    val publisher: ApplicationEventPublisher
) {
    // Chạy sau khi chuyến đã commit vào DB; fallbackExecution để vẫn chạy nếu nơi publish không có transaction
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handleRideRequested(event: RideRequested) {
        val id = event.rideId.toString()
        val radius = 2.0
        val nearbyDrivers: List<NearbyDriver> = locationService.findNearbyDrivers(event.pickup, radius)

        val rankedDrivers = selectionService.filterAndRankDrivers(nearbyDrivers, event.transmission)
        val rankedDriverIds = rankedDrivers.map { it.driverId.toString() }

        if (rankedDriverIds.isEmpty()) {
            publisher.publishEvent(RideNoDriverFound(event.rideId))
            return
        }

        // State trước, hàng đợi sau, rồi mới mời
        val statusKey = rideState(id)
        redisTemplate.opsForHash<String, String>().putAll(
            statusKey,
            mapOf(
                "status" to RideStatus.SEARCHING.name,
                "customerId" to event.customerId.toString(),
                "transmission" to event.transmission.name
            )
        )
        redisTemplate.expire(statusKey, Duration.ofHours(24))

        // Lua lấy bằng RPOP (cuối danh sách). leftPushAll đẩy từng phần tử vào đầu,
        // nên phần tử đầu tiên (xếp hạng tốt nhất) kết thúc ở cuối và được lấy ra trước.
        val queueKey = rideCandidates(id)
        redisTemplate.opsForList().leftPushAll(queueKey, rankedDriverIds)
        redisTemplate.expire(queueKey, Duration.ofHours(1))

        notificationService.sendOfferToNextDriver(event.rideId)
    }

    @EventListener
    fun handleRideCompleted(event: RideCompleted) {
        val statusKey = rideState(event.rideId.toString())
        redisTemplate.opsForHash<String, String>().put(statusKey, "status", "COMPLETED")
        redisTemplate.expire(statusKey, Duration.ofHours(1))
    }
}