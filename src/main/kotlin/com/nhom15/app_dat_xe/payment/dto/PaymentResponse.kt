package com.nhom15.app_dat_xe.payment.dto

import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import java.time.Instant

data class PaymentResponse(
    val id: Long,
    val bookingId: Long,
    val customerId: Long,
    val driverId: Long?,
    val amount: Long,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val paidAt: Instant?,
    val createdAt: Instant
)