package com.nhom15.app_dat_xe.payment.dto

import java.math.BigDecimal

data class WalletResponse(
    val driverId: Long,
    val balance: BigDecimal,
    val pendingPayout: BigDecimal,
    val grossIncome: BigDecimal,
    val netIncome: BigDecimal,
    val deductedFees: BigDecimal,
    val totalCompletedTrips: Long
)