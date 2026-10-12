package com.nhom15.app_dat_xe.ride.dto.response

import com.nhom15.app_dat_xe.common.enums.RideStatus
import java.time.Instant

data class CreateRideResponse(
    val id: Long,
    val code: String,
    val status: RideStatus,
    val estimatedFare: Long,
    val createdAt: Instant,
)
