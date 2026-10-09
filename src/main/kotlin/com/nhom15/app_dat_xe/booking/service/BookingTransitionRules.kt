package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.booking.entity.BookingTransitionEvent
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import com.nhom15.app_dat_xe.common.exception.ConflictException

internal object BookingTransitionRules {

    private val transitions = mapOf(
        (BookingStatus.PENDING to BookingTransitionEvent.START_SEARCH) to BookingStatus.SEARCHING,
        (BookingStatus.SEARCHING to BookingTransitionEvent.ASSIGN_DRIVER) to BookingStatus.DRIVER_ASSIGNED,
        (BookingStatus.DRIVER_ASSIGNED to BookingTransitionEvent.DRIVER_START_ARRIVING) to BookingStatus.DRIVER_ARRIVING,
        (BookingStatus.DRIVER_ARRIVING to BookingTransitionEvent.DRIVER_ARRIVED) to BookingStatus.DRIVER_ARRIVED,
        (BookingStatus.DRIVER_ARRIVED to BookingTransitionEvent.PICKUP_HANDOVER_CREATED) to BookingStatus.VEHICLE_HANDOVER,
        (BookingStatus.VEHICLE_HANDOVER to BookingTransitionEvent.START_TRIP) to BookingStatus.IN_PROGRESS,
        (BookingStatus.IN_PROGRESS to BookingTransitionEvent.ARRIVE_DESTINATION) to BookingStatus.ARRIVED_DESTINATION,
        (BookingStatus.ARRIVED_DESTINATION to BookingTransitionEvent.DROPOFF_HANDOVER_CREATED) to BookingStatus.VEHICLE_RETURNED,
        (BookingStatus.VEHICLE_RETURNED to BookingTransitionEvent.COMPLETE) to BookingStatus.COMPLETED,
        (BookingStatus.SEARCHING to BookingTransitionEvent.NO_DRIVER_FOUND) to BookingStatus.NO_DRIVER_FOUND,
        (BookingStatus.PENDING to BookingTransitionEvent.EXPIRE) to BookingStatus.EXPIRED,
    )

    private val cancellableStatuses = setOf(
        BookingStatus.PENDING,
        BookingStatus.SEARCHING,
        BookingStatus.DRIVER_ASSIGNED,
        BookingStatus.DRIVER_ARRIVING,
        BookingStatus.DRIVER_ARRIVED,
    )

    fun resolve(current: BookingStatus, event: BookingTransitionEvent): BookingStatus {
        if (event == BookingTransitionEvent.CANCEL && current in cancellableStatuses) {
            return BookingStatus.CANCELLED
        }

        return transitions[current to event]
            ?: throw ConflictException(
                ErrorCode.INVALID_BOOKING_STATE,
                "Không thể thực hiện $event khi booking đang ở trạng thái $current",
            )
    }
}
