package com.nhom15.app_dat_xe.payment.dto

import com.nhom15.app_dat_xe.payment.entity.DiscountType

data class VoucherResponse(
    val code: String,
    val discountType: DiscountType,
    val value: Long,
    val orderAmount: Long,
    val discountAmount: Long,
    val finalAmount: Long,
    val minimumOrderAmount: Long,
    val maxDiscount: Long?,
    val usageLimit: Int?,
    val usageCount: Int
)