package com.nhom15.app_dat_xe.booking.api.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalTime

data class CreatePricingRuleRequest(
    @field:PositiveOrZero
    val baseFare: Long,

    @field:PositiveOrZero
    val perKm: Long,

    @field:PositiveOrZero
    val perMinuteWaiting: Long,

    @field:Min(0)
    @field:Max(100)
    val nightSurchargePercent: Int = 0,

    val nightStart: LocalTime? = null,

    val nightEnd: LocalTime? = null,

    @field:PositiveOrZero
    val returnFeeForDriver: Long = 0,

    @field:PositiveOrZero
    val minFare: Long,

    val active: Boolean = true,
)

data class UpdatePricingRuleRequest(
    @field:PositiveOrZero
    val baseFare: Long,

    @field:PositiveOrZero
    val perKm: Long,

    @field:PositiveOrZero
    val perMinuteWaiting: Long,

    @field:Min(0)
    @field:Max(100)
    val nightSurchargePercent: Int,

    val nightStart: LocalTime? = null,

    val nightEnd: LocalTime? = null,

    @field:PositiveOrZero
    val returnFeeForDriver: Long,

    @field:PositiveOrZero
    val minFare: Long,

    val active: Boolean,
)
