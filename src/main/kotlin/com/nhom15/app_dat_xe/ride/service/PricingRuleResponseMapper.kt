package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.ride.dto.response.PricingRuleResponse
import com.nhom15.app_dat_xe.ride.entity.PricingRule

internal object PricingRuleResponseMapper {
    fun toResponse(rule: PricingRule) = PricingRuleResponse(
        id = rule.id,
        baseFare = rule.baseFare,
        perKm = rule.perKm,
        perMinuteWaiting = rule.perMinuteWaiting,
        nightSurchargePercent = rule.nightSurchargePercent,
        nightStart = rule.nightStart,
        nightEnd = rule.nightEnd,
        returnFeeForDriver = rule.returnFeeForDriver,
        minFare = rule.minFare,
        active = rule.active,
        createdAt = rule.createdAt,
        updatedAt = rule.updatedAt,
    )
}
