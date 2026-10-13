package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.ride.event.RideRequested
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import com.nhom15.app_dat_xe.ride.service.state.RideStateMachine
import com.nhom15.app_dat_xe.ride.service.state.RideTransitionCommand
import com.nhom15.app_dat_xe.ride.service.state.RideTransitionEvent
import com.nhom15.app_dat_xe.ride.service.state.SYSTEM_RIDE_ACTOR
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant

@Component
class ScheduledRideScheduler(
    private val rideRepository: RideRepository,
    private val stateMachine: RideStateMachine,
    private val eventPublisher: EventPublisher,
    transactionManager: PlatformTransactionManager,
    @Value("\${ride.scheduling.dispatch-lead-minutes:15}") private val dispatchLeadMinutes: Long,
    @Value("\${ride.scheduling.expire-grace-minutes:30}") private val expireGraceMinutes: Long,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @Scheduled(fixedDelayString = "\${ride.scheduling.poll-interval-ms:30000}")
    fun processDueRides() {
        val now = Instant.now()
        val dueRides = rideRepository.findByStatusAndScheduledAtLessThanEqual(
            RideStatus.PENDING,
            now.plus(Duration.ofMinutes(dispatchLeadMinutes)),
        )

        dueRides.forEach { candidate ->
            try {
                transactionTemplate.executeWithoutResult {
                    val ride = rideRepository.findById(candidate.id).orElse(null) ?: return@executeWithoutResult
                    if (ride.status != RideStatus.PENDING) return@executeWithoutResult
                    val scheduledAt = ride.scheduledAt ?: return@executeWithoutResult
                    val currentTime = Instant.now()

                    if (scheduledAt.isBefore(currentTime.minus(Duration.ofMinutes(expireGraceMinutes)))) {
                        stateMachine.transition(
                            RideTransitionCommand(
                                rideId = ride.id,
                                event = RideTransitionEvent.EXPIRE,
                                actor = SYSTEM_RIDE_ACTOR,
                                note = "Hết thời gian tìm tài xế cho chuyến đặt lịch",
                            ),
                        )
                        return@executeWithoutResult
                    }

                    val searchingRide = stateMachine.transition(
                        RideTransitionCommand(
                            rideId = ride.id,
                            event = RideTransitionEvent.START_SEARCH,
                            actor = SYSTEM_RIDE_ACTOR,
                            note = "Đến thời điểm bắt đầu tìm tài xế cho chuyến đặt lịch",
                        ),
                    )
                    eventPublisher.publish(
                        RideRequested(
                            rideId = searchingRide.id,
                            customerId = searchingRide.customerId,
                            pickup = searchingRide.pickup.geoPoint,
                            vehicleType = searchingRide.vehicleType,
                            transmission = searchingRide.transmission,
                            scheduledAt = searchingRide.scheduledAt,
                        ),
                    )
                }
            } catch (exception: Exception) {
                log.warn("Không xử lý được ride đặt lịch {}: {}", candidate.id, exception.message)
            }
        }
    }
}
