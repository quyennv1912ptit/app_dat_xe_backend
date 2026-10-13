package com.nhom15.app_dat_xe.ride.dto.response

import java.time.Instant
import java.time.LocalTime
import com.nhom15.app_dat_xe.common.enums.VehicleType

data class PricingRuleResponse(
    val id: Long,
    val baseFare: Long,
    val perKm: Long,
    val perMinuteWaiting: Long,
    val nightSurchargePercent: Int,
    val nightStart: LocalTime?,
    val nightEnd: LocalTime?,
    val returnFeeForDriver: Long,
    val minFare: Long,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val vehicleType: VehicleType? = null,
)
