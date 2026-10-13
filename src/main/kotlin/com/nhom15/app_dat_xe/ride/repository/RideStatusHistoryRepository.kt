package com.nhom15.app_dat_xe.ride.repository

import com.nhom15.app_dat_xe.ride.entity.RideStatusHistory
import org.springframework.data.jpa.repository.JpaRepository

interface RideStatusHistoryRepository : JpaRepository<RideStatusHistory, Long> {
    fun findAllByRideIdOrderByCreatedAtAsc(rideId: Long): List<RideStatusHistory>

    fun findAllByRideIdOrderByCreatedAtAscIdAsc(rideId: Long): List<RideStatusHistory>
}
