package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideCandidates
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideRejected
import com.nhom15.app_dat_xe.common.redis.RedisKeys.rideState
import com.nhom15.app_dat_xe.common.redis.RedisTtl
import com.nhom15.app_dat_xe.matching.config.MatchingProperties
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

@Service
class RideDispatchService(
    private val locationService: LocationService,
    private val selectionService: DriverSelectionService,
    private val matchingProperties: MatchingProperties,
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
        val statusKey = rideState(id)
        redisTemplate.opsForHash<String, String>().putAll(
            statusKey,
            mapOf(
                "status" to RideStatus.SEARCHING.name,
                "customerId" to event.customerId.toString(),
                "transmission" to event.transmission.name,
                "pickupLat" to event.pickup.lat.toString(),
                "pickupLng" to event.pickup.lng.toString(),
                "nextRoundIndex" to "0",
            )
        )
        redisTemplate.expire(statusKey, RedisTtl.RIDE_STATE)

        sendOfferToNextDriver(event.rideId)
    }

    fun sendOfferToNextDriver(rideId: Long) {
        val id = rideId.toString()
        val keys = listOf(rideState(id), rideCandidates(id), rideRejected(id), RedisKeys.DISPATCH_TIMEOUTS)

        while (true) {
            val deadline = System.currentTimeMillis() + RedisTtl.OFFER_TIMEOUT.toMillis()

            val next = redisTemplate.execute(
                DispatchScripts.OFFER_NEXT,
                keys,
                id,
                deadline.toString(),
                RedisTtl.DRIVER_LOCK.seconds.toString(),
                RedisTtl.RIDE_STATE.seconds.toString(),
            ) ?: return

            when (next) {
                "NOT_SEARCHING" -> return
                "WAIT" -> return
                "ROUND_EXHAUSTED" -> {
                    when (prepareNextRound(id, rideId)) {
                        RoundPreparation.RETRY -> continue
                        RoundPreparation.WAIT, RoundPreparation.STOP -> return
                    }
                }
            }

            val driverIdStr = next
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

    private fun prepareNextRound(id: String, rideId: Long): RoundPreparation {
        val claim = redisTemplate.execute(
            DispatchScripts.ADVANCE_ROUND,
            listOf(rideState(id), rideCandidates(id)),
            *matchingProperties.searchRadiiKm.map(Double::toString).toTypedArray(),
        ) ?: return RoundPreparation.STOP

        when (claim) {
            "WAIT" -> return RoundPreparation.WAIT
            "NOT_SEARCHING" -> return RoundPreparation.STOP
            "FINISHED" -> {
                publisher.publishEvent(RideNoDriverFound(rideId))
                return RoundPreparation.STOP
            }
        }

        val parts = claim.split(':', limit = 2)
        val roundIndex = parts.getOrNull(0)?.toIntOrNull() ?: return RoundPreparation.STOP
        val radiusKm = parts.getOrNull(1)?.toDoubleOrNull() ?: return RoundPreparation.STOP
        val state = redisTemplate.opsForHash<String, String>().entries(rideState(id))
        val pickupLat = state["pickupLat"]?.toDoubleOrNull()
        val pickupLng = state["pickupLng"]?.toDoubleOrNull()
        val transmission = state["transmission"]?.let { runCatching { Transmission.valueOf(it) }.getOrNull() }
        if (pickupLat == null || pickupLng == null || transmission == null) return RoundPreparation.STOP

        val triedIds = redisTemplate.opsForSet().members(rideRejected(id)).orEmpty()
        // Redis returns nearest-first. Account for already tried IDs so the next untried
        // candidates are still included when the radius query is capped.
        val queryLimit = matchingProperties.maxCandidatesPerRound.toLong() + triedIds.size.toLong()
        val nearbyDrivers: List<NearbyDriver> = locationService.findNearbyDrivers(
            GeoPoint(lat = pickupLat, lng = pickupLng),
            radiusKm,
            queryLimit,
        )
        val rankedDriverIds = selectionService
            .filterAndRankDrivers(nearbyDrivers, transmission, radiusKm)
            .asSequence()
            .filterNot { it.driverId.toString() in triedIds }
            .take(matchingProperties.maxCandidatesPerRound)
            .map { it.driverId.toString() }
            .toList()

        val completed = redisTemplate.execute(
            DispatchScripts.COMPLETE_ROUND,
            listOf(rideState(id), rideCandidates(id)),
            roundIndex.toString(),
            RedisTtl.RIDE_CANDIDATES.seconds.toString(),
            *rankedDriverIds.toTypedArray(),
        ) == 1L
        return if (completed) RoundPreparation.RETRY else RoundPreparation.STOP
    }

    private enum class RoundPreparation { RETRY, WAIT, STOP }
}
