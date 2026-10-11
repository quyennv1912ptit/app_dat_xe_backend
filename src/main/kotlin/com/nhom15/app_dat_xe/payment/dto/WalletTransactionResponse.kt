package com.nhom15.app_dat_xe.payment.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class WalletTransactionResponse(
    val id: Long,
    val bookingId: Long?,
    val amount: BigDecimal,
    val type: String,
    val status: String,
    val description: String?,
    val createdAt: LocalDateTime?
)