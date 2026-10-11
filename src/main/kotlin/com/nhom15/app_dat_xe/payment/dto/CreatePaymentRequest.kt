package com.nhom15.app_dat_xe.payment.dto

import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import jakarta.validation.constraints.NotNull

data class CreatePaymentRequest(

    @field:NotNull
    val bookingId: Long?,

    @field:NotNull
    val method: PaymentMethod?
)