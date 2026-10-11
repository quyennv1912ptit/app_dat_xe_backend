
package com.nhom15.app_dat_xe.payment.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class WithdrawalResponse(
    val transactionId: Long,
    val driverId: Long,
    val amount: BigDecimal,
    val status: String,
    val description: String?,
    val createdAt: LocalDateTime?
)