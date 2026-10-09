package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.common.port.BookingAssignmentPort
import com.nhom15.app_dat_xe.common.port.BookingAssignmentResult
import org.springframework.stereotype.Service

@Service
class BookingAssignmentService(
    private val bookingStateMachine: BookingStateMachine,
) : BookingAssignmentPort {

    override fun assignDriver(bookingId: Long, driverId: Long): BookingAssignmentResult? =
        bookingStateMachine.tryAssignDriver(bookingId, driverId)
}
