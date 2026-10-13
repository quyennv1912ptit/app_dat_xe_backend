package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideCandidates
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideRejected
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideState
import com.nhom15.app_dat_xe.common.redis.RedisTtl
import com.nhom15.app_dat_xe.notification.api.DeliveryResult
import com.nhom15.app_dat_xe.notification.api.NotificationPort
import com.nhom15.app_dat_xe.notification.api.Recipient
import com.nhom15.app_dat_xe.ride.event.RideNoDriverFound
import com.nhom15.app_dat_xe.ride.event.RideRequested
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver
import com.nhom15.app_dat_xe.tracking.service.LocationService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.Duration

@Service
class RideDispatchService(
    private val locationService: LocationService,
    private val selectionService: DriverSelectionService,
    private val redisTemplate: StringRedisTemplate,
    private val publisher: ApplicationEventPublisher,
    private val rideOfferBuilder: RideOfferBuilder,
    private val notificationPort: NotificationPort,
    private val releaseService: OfferReleaseService
) {
    private val log = LoggerFactory.getLogger(javaClass)

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

        val statusKey = rideState(id)
        redisTemplate.opsForHash<String, String>().putAll(
            statusKey,
            mapOf(
                "status" to RideStatus.SEARCHING.name,
                "customerId" to event.customerId.toString(),
                "transmission" to event.transmission.name
            )
        )
        redisTemplate.expire(statusKey, RedisTtl.RIDE_STATE)

        val queueKey = rideCandidates(id)
        redisTemplate.opsForList().leftPushAll(queueKey, rankedDriverIds)
        redisTemplate.expire(queueKey, RedisTtl.RIDE_CANDIDATES)

        sendOfferToNextDriver(event.rideId)
    }

    fun sendOfferToNextDriver(rideId: Long) {
        val id = rideId.toString()
        val keys = listOf(rideState(id), rideCandidates(id), rideRejected(id), RedisKeys.DISPATCH_TIMEOUTS)

        while (true) {
            val deadline = System.currentTimeMillis() + RedisTtl.OFFER_TIMEOUT.toMillis()

            val driverIdStr = redisTemplate.execute(
                DispatchScripts.OFFER_NEXT, keys, id, deadline.toString(), RedisTtl.DRIVER_LOCK.seconds.toString()
            ) ?: run {
                val status = redisTemplate.opsForHash<String, String>().get(rideState(id), "status")
                if (status == "NO_DRIVER_FOUND") publisher.publishEvent(RideNoDriverFound(rideId))
                return
            }
            val driverId = driverIdStr.toLong()

            val delivered = try {
                val payload = rideOfferBuilder.build(rideId, driverId, deadline)
                notificationPort.send(Recipient.Driver(driverId), payload) != DeliveryResult.UNREACHABLE
            } catch (e: Exception) {
                log.warn("Không dựng/gửi được offer chuyến {} cho tài xế {}: {}", rideId, driverId, e.message)
                false
            }

            if (delivered) {
                redisTemplate.opsForGeo().remove(RedisKeys.GEO_KEY, driverIdStr)
                return
            }

            if (!releaseService.release(rideId, driverId, "unreachable")) return
        }
    }
}