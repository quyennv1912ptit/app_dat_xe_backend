package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.ride.event.RideDriverAssigned
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RideAcceptanceService(
    private val redisTemplate: StringRedisTemplate,
    private val eventPublisher: EventPublisher,
    private val dispatchService: RideDispatchService,
    private val releaseService: OfferReleaseService
) {

    fun accept(rideId: Long, driverId: Long): Boolean {
        val id = rideId.toString()

        val accepted = redisTemplate.execute(
            DispatchScripts.ACCEPT,
            listOf(
                RedisKeys.rideState(id),
                RedisKeys.DISPATCH_TIMEOUTS,
                RedisKeys.rideCandidates(id)
            ),
            driverId.toString(),
            id,
            System.currentTimeMillis().toString()
        ) == 1L

        if (!accepted) return false

        val customerId = redisTemplate.opsForHash<String, String>()
            .get(RedisKeys.rideState(id), "customerId")
            ?.toLongOrNull()
            ?: error("Thiếu customerId trong state của chuyến $rideId")

        eventPublisher.publish(
            RideDriverAssigned(
                rideId = rideId,
                customerId = customerId,
                driverId = driverId
            )
        )

        return true
    }

    fun reject(rideId: Long, driverId: Long) {
        if (releaseService.release(rideId, driverId, "rejected")) {
            dispatchService.sendOfferToNextDriver(rideId)
        }
    }
}