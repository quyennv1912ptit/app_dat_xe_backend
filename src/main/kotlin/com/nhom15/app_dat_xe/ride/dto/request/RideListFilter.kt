package com.nhom15.app_dat_xe.ride.dto.request

import com.nhom15.app_dat_xe.common.enums.RideStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero

data class RideListFilter(
    val status: RideStatus? = null,

    @field:PositiveOrZero
    val page: Int = 0,

    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
