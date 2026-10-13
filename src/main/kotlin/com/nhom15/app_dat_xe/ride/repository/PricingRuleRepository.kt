package com.nhom15.app_dat_xe.ride.repository

import com.nhom15.app_dat_xe.ride.entity.PricingRule
import org.springframework.data.jpa.repository.JpaRepository

interface PricingRuleRepository : JpaRepository<PricingRule, Long> {
    fun findFirstByActiveTrueOrderByCreatedAtDesc(): PricingRule?
}
