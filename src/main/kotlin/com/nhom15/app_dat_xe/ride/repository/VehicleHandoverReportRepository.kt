package com.nhom15.app_dat_xe.ride.repository

import com.nhom15.app_dat_xe.ride.entity.HandoverPhase
import com.nhom15.app_dat_xe.ride.entity.VehicleHandoverReport
import org.springframework.data.jpa.repository.JpaRepository

interface VehicleHandoverReportRepository : JpaRepository<VehicleHandoverReport, Long> {
    fun findByRideIdAndPhase(rideId: Long, phase: HandoverPhase): VehicleHandoverReport?

    fun existsByRideIdAndPhase(rideId: Long, phase: HandoverPhase): Boolean
}
