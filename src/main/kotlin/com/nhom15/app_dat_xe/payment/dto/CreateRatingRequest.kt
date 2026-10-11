// Chứa dữ liệu đầu vào khi người dùng gửi đánh giá cho một chuyến đi.

package com.nhom15.app_dat_xe.payment.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateRatingRequest(

    @field:NotNull
    val bookingId: Long?,

    @field:NotNull
    val rateeId: Long?,

    @field:Min(1)
    @field:Max(5)
    val stars: Int,

    val tags: String? = null,

    val comment: String? = null,

    @field:NotNull
    val direction: String?
)