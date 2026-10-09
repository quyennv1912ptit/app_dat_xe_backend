package com.nhom15.app_dat_xe.booking.repository

import com.nhom15.app_dat_xe.booking.entity.BookingStatusHistory
import org.springframework.data.jpa.repository.JpaRepository

interface BookingStatusHistoryRepository : JpaRepository<BookingStatusHistory, Long> {
    fun findAllByBookingIdOrderByCreatedAtAsc(bookingId: Long): List<BookingStatusHistory>

    fun findAllByBookingIdOrderByCreatedAtAscIdAsc(bookingId: Long): List<BookingStatusHistory>
}
