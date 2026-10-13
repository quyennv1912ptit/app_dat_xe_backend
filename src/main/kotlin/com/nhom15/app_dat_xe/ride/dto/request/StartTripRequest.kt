package com.nhom15.app_dat_xe.ride.dto.request

import jakarta.validation.constraints.Size

data class StartTripRequest(
    @field:Size(max = 1_000)
    val note: String? = null,
)
