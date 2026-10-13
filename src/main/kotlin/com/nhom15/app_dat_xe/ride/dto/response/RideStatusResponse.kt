package com.nhom15.app_dat_xe.ride.dto.response

import com.nhom15.app_dat_xe.common.enums.RideStatus
import java.time.Instant

data class RideStatusResponse(
    val rideId: Long,
    val previousStatus: RideStatus,
    val currentStatus: RideStatus,
    val changedAt: Instant,
)
