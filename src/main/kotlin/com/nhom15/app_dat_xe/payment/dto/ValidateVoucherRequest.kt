package com.nhom15.app_dat_xe.payment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class ValidateVoucherRequest(

    @field:NotBlank
    val code: String,

    @field:NotNull
    @field:Positive
    val userId: Long?,

    @field:NotNull
    @field:Positive
    val orderAmount: Long?
)