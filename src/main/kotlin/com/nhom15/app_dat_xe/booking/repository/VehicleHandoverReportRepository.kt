package com.nhom15.app_dat_xe.booking.repository

import com.nhom15.app_dat_xe.booking.entity.HandoverPhase
import com.nhom15.app_dat_xe.booking.entity.VehicleHandoverReport
import org.springframework.data.jpa.repository.JpaRepository

interface VehicleHandoverReportRepository : JpaRepository<VehicleHandoverReport, Long> {
    fun findByBookingIdAndPhase(bookingId: Long, phase: HandoverPhase): VehicleHandoverReport?

    fun existsByBookingIdAndPhase(bookingId: Long, phase: HandoverPhase): Boolean
}
