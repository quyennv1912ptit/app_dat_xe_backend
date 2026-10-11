package com.nhom15.app_dat_xe.notification.service

import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideCandidates
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideRejected
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideState
import com.nhom15.app_dat_xe.common.websocket.PayloadType
import com.nhom15.app_dat_xe.matching.service.DispatchScripts
import com.nhom15.app_dat_xe.matching.service.RideOfferBuilder
import com.nhom15.app_dat_xe.ride.event.RideNoDriverFound
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val redisTemplate: StringRedisTemplate,
    private val rideOfferBuilder: RideOfferBuilder,
    private val notifier: StompDriverNotifier,
    private val publisher: ApplicationEventPublisher
) {
    fun sendOfferToNextDriver(rideId: Long) {
        val id = rideId.toString()
        val keys = listOf(rideState(id), rideCandidates(id), rideRejected(id), RedisKeys.DISPATCH_TIMEOUTS)

        while (true) {
            // Một mốc hạn duy nhất: ghi vào ZSET (trong Lua) và gửi cho app tài xế làm expiresAt
            val deadline = System.currentTimeMillis() + OFFER_TIMEOUT_MS

            val driverId = redisTemplate.execute(
                DispatchScripts.OFFER_NEXT, keys, id, deadline.toString()
            ) ?: run {
                // Lua trả null: hết ứng viên, hoặc chuyến không còn SEARCHING (đã nhận / đã huỷ)
                val status = redisTemplate.opsForHash<String, String>().get(rideState(id), "status")
                if (status == "NO_DRIVER_FOUND") publisher.publishEvent(RideNoDriverFound(rideId))
                return
            }

            // Dựng lời mời TRƯỚC khi rút tài xế khỏi GEO (builder đọc vị trí từ GEO)
            val payload = try {
                rideOfferBuilder.build(rideId, driverId.toLong(), deadline)
            } catch (e: NotFoundException) {
                undoReservation(id, driverId)
                continue
            }

            redisTemplate.opsForGeo().remove(RedisKeys.GEO_KEY, driverId)
            notifier.sendToDriver(driverId.toLong(), PayloadType.RIDE_OFFER, payload)
            return
        }
    }

    fun sendOfferExpired(driverId: Long, rideId: Long) {
        notifier.sendToDriver(driverId, PayloadType.OFFER_EXPIRED, mapOf("rideId" to rideId))
    }

    /** Lua đã giữ chỗ tài xế (khoá, OFFERED, ZSET) nhưng không gửi được lời mời: hoàn tác rồi thử người kế. */
    private fun undoReservation(rideId: String, driverId: String) {
        redisTemplate.opsForSet().add(rideRejected(rideId), driverId)
        redisTemplate.opsForZSet().remove(RedisKeys.DISPATCH_TIMEOUTS, "$rideId:$driverId")
        redisTemplate.delete(RedisKeys.driverLock(driverId))
        redisTemplate.opsForHash<String, String>().increment(RedisKeys.driverStats(driverId), "offered", -1)

        // Chưa rút khỏi GEO nên không cần thêm lại; chỉ trả trạng thái
        val alive = redisTemplate.hasKey(RedisKeys.driverAlive(driverId)) == true
        redisTemplate.opsForValue().set(RedisKeys.driverStatus(driverId), if (alive) "ONLINE" else "OFFLINE")
    }

    companion object {
        const val OFFER_TIMEOUT_MS = 15_000L
    }
}