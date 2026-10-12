package com.nhom15.app_dat_xe.ride.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.nhom15.app_dat_xe.common.enums.RideStatus
import java.time.Instant

data class RideStatusResponse(
    @get:JsonProperty("bookingId")
    val rideId: Long,
    val code: String,
    val previousStatus: RideStatus,
    val currentStatus: RideStatus,
    val changedAt: Instant,
)
