package com.nhom15.app_dat_xe.ride.dto.internal

import java.math.BigDecimal

data class RideQuote(
    val pricingRuleId: Long,
    val distanceKm: BigDecimal,
    val estimatedDurationMinutes: Long,
    val baseFare: Long,
    val distanceFare: Long,
    val waitingFare: Long,
    val nightSurcharge: Long,
    val returnFee: Long,
    val estimatedFare: Long,
)
