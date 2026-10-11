package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.entity.VoucherUsage
import org.springframework.data.jpa.repository.JpaRepository

interface VoucherUsageRepository : JpaRepository<VoucherUsage, Long> {

    fun existsByVoucherIdAndUserId(
        voucherId: Long,
        userId: Long
    ): Boolean

    fun findByVoucherIdAndUserId(
        voucherId: Long,
        userId: Long
    ): VoucherUsage?
}