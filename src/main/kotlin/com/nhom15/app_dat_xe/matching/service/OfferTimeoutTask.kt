package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.notification.service.NotificationService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OfferTimeoutTask(
    private val redisTemplate: StringRedisTemplate,
    private val acceptanceService: RideAcceptanceService,
    private val notificationService: NotificationService
) {
    @Scheduled(fixedDelay = 1000)
    fun handleExpiredOffers() {
        val now = System.currentTimeMillis().toDouble()
        val due = redisTemplate.opsForZSet().rangeByScore(RedisKeys.DISPATCH_TIMEOUTS, 0.0, now) ?: return

        due.forEach { member ->
            // remove trả 1 = instance này giành được; nhiều instance cùng quét cũng không xử lý trùng
            if (redisTemplate.opsForZSet().remove(RedisKeys.DISPATCH_TIMEOUTS, member) == 1L) {
                val (rideId, driverId) = member.split(":").map { it.toLong() }
                if (acceptanceService.releaseOffer(rideId, driverId, "timeout")) {
                    notificationService.sendOfferExpired(driverId, rideId)
                    notificationService.sendOfferToNextDriver(rideId)
                }
            }
        }
    }
}