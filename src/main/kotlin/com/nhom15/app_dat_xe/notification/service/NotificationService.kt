package com.nhom15.app_dat_xe.notification.service

import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.ride.repository.RidesRepository
import com.nhom15.app_dat_xe.common.websocket.PayloadType
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideCandidates
import com.nhom15.app_dat_xe.common.websocket.RideOfferPayload
import com.nhom15.app_dat_xe.common.websocket.WsEnvelope
import com.nhom15.app_dat_xe.matching.dto.RideOfferPayload
import com.nhom15.app_dat_xe.matching.service.RideOfferBuilder
import com.nhom15.app_dat_xe.tracking.service.MapService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class NotificationService (
    private val messaging: SimpMessagingTemplate,
    private val redisTemplate: StringRedisTemplate,
    private val mapService: MapService,
    private val ridesRepository: RidesRepository,
    private val rideOfferBuilder: RideOfferBuilder,
    private val notifier: StompDriverNotifier
) {
    fun sendOfferToNextDriver(rideId: Long) {
        val id = rideId.toString()

        while (true) {
            // Lua: kiểm tra chuyến còn SEARCHING, bỏ qua tài xế trong rejected, ghi currentDriver, lấy người gần nhất
            val driverId = redisTemplate.execute(
                DispatchScripts.OFFER_NEXT,
                listOf(RedisKeys.rideState(id), RedisKeys.rideCandidates(id), RedisKeys.rideRejected(id))
            ) ?: return   // hết tài xế, hoặc chuyến đã có người nhận / bị huỷ

            val deadline = System.currentTimeMillis() + OFFER_TIMEOUT_MS

            // Dựng lời mời TRƯỚC khi rút tài xế khỏi GEO (builder đọc vị trí từ GEO).
            // Không dựng được (mất vị trí...) thì đánh dấu bỏ qua và thử người kế, không để chuyến kẹt.
            val payload = try {
                rideOfferBuilder.build(rideId, driverId.toLong(), deadline)
            } catch (e: NotFoundException) {
                redisTemplate.opsForSet().add(RedisKeys.rideRejected(id), driverId)
                continue
            }

            // TODO: giành khoá driver:{id}:lock, đổi status tài xế sang OFFERED, removeFromPool (như bản trước)

            // Cùng một mốc deadline cho job quét timeout và cho app tài xế
            redisTemplate.opsForZSet().add(RedisKeys.DISPATCH_TIMEOUTS, "$rideId:$driverId", deadline.toDouble())

            notifier.sendToDriver(driverId.toLong(), PayloadType.RIDE_OFFER, payload)
            return
        }
    }

    companion object {
        const val OFFER_TIMEOUT_MS = 15_000L
    }


}

object DispatchScripts {
    // Lấy tài xế tiếp theo. Trả null nếu ride không còn SEARCHING hoặc hết tài xế
    val OFFER_NEXT = DefaultRedisScript<String>(
        """
        if redis.call('HGET', KEYS[1], 'status') ~= 'SEARCHING' then return nil end
        local d = redis.call('RPOP', KEYS[2])
        if not d then
            redis.call('HSET', KEYS[1], 'status', 'NO_DRIVER')
            return nil
        end
        redis.call('HSET', KEYS[1], 'currentDriver', d)
        return d
    """.trimIndent(), String::class.java
    )

    // Tài xế nhận: chỉ thành công nếu đang SEARCHING và đúng là người được mời
    val ACCEPT = DefaultRedisScript<Long>("""
        if redis.call('HGET', KEYS[1], 'status') == 'SEARCHING'
           and redis.call('HGET', KEYS[1], 'currentDriver') == ARGV[1] then
            redis.call('HSET', KEYS[1], 'status', 'ACCEPTED', 'driverId', ARGV[1])
            return 1
        end
        return 0
    """.trimIndent(), Long::class.java)
}