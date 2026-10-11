// Chứa dữ liệu Rating trả về cho phía client.

package com.nhom15.app_dat_xe.payment.dto

import java.time.Instant

data class RatingResponse(
    val id: Long,
    val bookingId: Long,
    val raterId: Long,
    val rateeId: Long,
    val stars: Int,
    val tags: String?,
    val comment: String?,
    val direction: String,
    val createdAt: Instant
)