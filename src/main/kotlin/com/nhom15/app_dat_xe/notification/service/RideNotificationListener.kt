package com.nhom15.app_dat_xe.notification.service

import com.nhom15.app_dat_xe.account.repository.DriversRepository
import com.nhom15.app_dat_xe.common.enums.ActorRole
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.notification.api.NotificationPort
import com.nhom15.app_dat_xe.notification.api.Recipient
import com.nhom15.app_dat_xe.notification.dto.*
import com.nhom15.app_dat_xe.ride.event.RideStatusChanged
import com.nhom15.app_dat_xe.ride.repository.RidesRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class RideNotificationListener(
    private val notificationPort: NotificationPort,
    private val driversRepository: DriversRepository,
    private val ridesRepository: RidesRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onRideStatusChanged(event: RideStatusChanged) {
        when (event.to) {
            RideStatus.DRIVER_ASSIGNED -> notifyDriverAssigned(event)

            RideStatus.DRIVER_ARRIVING,
            RideStatus.DRIVER_ARRIVED,
            RideStatus.VEHICLE_HANDOVER,
            RideStatus.IN_PROGRESS,
            RideStatus.ARRIVED_DESTINATION,
            RideStatus.VEHICLE_RETURNED -> notifyRideUpdated(event)

            RideStatus.COMPLETED -> notifyRideCompleted(event)
            RideStatus.CANCELLED -> notifyRideCancelled(event)
            RideStatus.NO_DRIVER_FOUND, RideStatus.EXPIRED -> notifyNoDriver(event)
            RideStatus.PENDING, RideStatus.SEARCHING -> Unit
        }
    }

    private fun notifyDriverAssigned(event: RideStatusChanged) {
        val driverId = event.driverId ?: return
        val driver = driversRepository.findByIdOrNull(driverId) ?: run {
            log.warn("Không tìm thấy tài xế {} khi báo chuyến {}", driverId, event.rideId)
            return
        }

        val payload = RideAssignedPayload(
            rideId = event.rideId,
            driver = DriverBrief(
                id = driverId,
                name = driver.fullName,
                phone = driver.phoneNumber,
                rating = driver.averageRating,
                avatarUrl = driver.avatarUrl
            )
        )
        notificationPort.send(Recipient.User(event.customerId), payload)
    }

    private fun notifyRideUpdated(event: RideStatusChanged) {
        val payload = RideUpdatedPayload(event.rideId, event.to)
        if (event.actor != ActorRole.USER) {
            notificationPort.send(Recipient.User(event.customerId), payload)
        }
        if (event.actor != ActorRole.DRIVER) {
            event.driverId?.let { notificationPort.send(Recipient.Driver(it), payload) }
        }
    }

    private fun notifyRideCompleted(event: RideStatusChanged) {
        val ride = ridesRepository.findByIdOrNull(event.rideId) ?: run {
            log.warn("Không tìm thấy chuyến với id {}", event.rideId)
            return
        }

        val payload = RideCompletedPayload(event.rideId, ride.finalFare ?: 0, ride.distanceKm)
        notificationPort.send(Recipient.User(event.customerId), payload)
    }

    private fun notifyRideCancelled(event: RideStatusChanged) {
        val payload = RideCancelledPayload(event.rideId, event.reason)

        if (event.actor != ActorRole.USER) {
            notificationPort.send(Recipient.User(event.customerId), payload)
        }
        if (event.actor != ActorRole.DRIVER) {
            event.driverId?.let { notificationPort.send(Recipient.Driver(it), payload) }
        }
    }

    private fun notifyNoDriver(event: RideStatusChanged) {
        val payload = RideNoDriverPayload(event.rideId, event.to)
        notificationPort.send(Recipient.User(event.customerId), payload)

        if (event.to == RideStatus.EXPIRED) {
            event.driverId?.let { notificationPort.send(Recipient.Driver(it), payload) }
        }
    }
}