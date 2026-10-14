package com.nhom15.app_dat_xe.ride.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class CreateRideRatingRequest(
    @field:Min(1)
    @field:Max(5)
    val stars: Int,
    @field:Size(max = 2000)
    val comment: String? = null,
    @field:PositiveOrZero
    val tip: Long = 0,
)

data class RideRatingResponse(
    val rideId: Long,
    val driverId: Long,
    val stars: Int,
    val comment: String?,
    val tip: Long,
)
