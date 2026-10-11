package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.notification.service.NotificationService
import com.nhom15.app_dat_xe.ride.event.RideDriverAssigned
import com.nhom15.app_dat_xe.ride.service.RideService
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RideAcceptanceService(
    private val redisTemplate: StringRedisTemplate,
    private val eventPublisher: EventPublisher,
    private val notificationService: NotificationService
) {
    /** Tài xế bấm nhận. false = lời mời đã hết hạn hoặc chuyến đã giao người khác (controller trả 410). */
    fun accept(rideId: Long, driverId: Long): Boolean {
        val id = rideId.toString()
        val ok = redisTemplate.execute(
            DispatchScripts.ACCEPT,
            listOf(RedisKeys.rideState(id), RedisKeys.DISPATCH_TIMEOUTS, RedisKeys.rideCandidates(id)),
            driverId.toString(), id, System.currentTimeMillis().toString()
        ) == 1L

        if (!ok) return false

        val customerId = redisTemplate.opsForHash<String, String>()
            .get(RedisKeys.rideState(id), "customerId")!!.toLong()   // đã ghi trong handleRideRequested
        eventPublisher.publish(RideDriverAssigned(rideId, customerId, driverId))
        return true
    }

    /** Tài xế từ chối. */
    fun reject(rideId: Long, driverId: Long) {
        if (releaseOffer(rideId, driverId, "rejected")) {
            notificationService.sendOfferToNextDriver(rideId)
        }
    }

    /**
     * Giải phóng tài xế sau từ chối hoặc hết hạn (OfferTimeoutTask cũng gọi hàm này).
     * Trả true nếu đúng là lời mời hiện tại; lời mời cũ hoặc đã nhận rồi thì bỏ qua.
     */
    fun releaseOffer(rideId: Long, driverId: Long, stat: String): Boolean {
        val id = rideId.toString()
        return redisTemplate.execute(
            DispatchScripts.RELEASE,
            listOf(RedisKeys.rideState(id), RedisKeys.DISPATCH_TIMEOUTS, RedisKeys.rideRejected(id), RedisKeys.GEO_KEY),
            driverId.toString(), id, stat
        ) == 1L
    }

    /** Tài xế rảnh lại: ONLINE và quay về GEO. Nếu đã mất kết nối thì đặt OFFLINE. */
    private fun returnToPool(driverId: String) {
        if (redisTemplate.hasKey(RedisKeys.driverAlive(driverId)) != true) {
            redisTemplate.opsForValue().set(RedisKeys.driverStatus(driverId), "OFFLINE")
            return
        }
        redisTemplate.opsForValue().set(RedisKeys.driverStatus(driverId), "ONLINE")
        val loc = redisTemplate.opsForHash<String, String>().entries(RedisKeys.driverLocation(driverId))
        val lat = loc["lat"]?.toDoubleOrNull() ?: return
        val lng = loc["lng"]?.toDoubleOrNull() ?: return
        redisTemplate.opsForGeo().add(RedisKeys.GEO_KEY, Point(lng, lat), driverId)
    }
}