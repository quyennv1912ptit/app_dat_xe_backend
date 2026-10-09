package com.nhom15.app_dat_xe.booking.api.dto.request

import com.nhom15.app_dat_xe.booking.entity.HandoverPhotoAngle
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class HandoverPhotoRequest(
    val angle: HandoverPhotoAngle,

    @field:NotBlank
    @field:Size(max = 2_048)
    val fileUrl: String,
)

data class CreateHandoverReportRequest(
    @field:PositiveOrZero
    val odometer: Long,

    @field:PositiveOrZero
    @field:Max(100)
    val fuelLevel: Int? = null,

    @field:Size(max = 2_000)
    val notes: String? = null,

    @field:Valid
    @field:Size(min = 4, max = 5)
    val photos: List<HandoverPhotoRequest>,
)

data class ConfirmHandoverRequest(
    @field:AssertTrue(message = "Khách hàng phải xác nhận biên bản bàn giao")
    val confirmed: Boolean,
)
