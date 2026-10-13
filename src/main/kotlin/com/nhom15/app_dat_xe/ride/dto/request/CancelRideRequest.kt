package com.nhom15.app_dat_xe.ride.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CancelRideRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val reason: String,
)
