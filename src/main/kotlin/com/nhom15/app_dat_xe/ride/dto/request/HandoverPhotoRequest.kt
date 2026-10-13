package com.nhom15.app_dat_xe.ride.dto.request

import com.nhom15.app_dat_xe.ride.entity.HandoverPhotoAngle
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class HandoverPhotoRequest(
    val angle: HandoverPhotoAngle,

    @field:NotBlank
    @field:Size(max = 2_048)
    val fileUrl: String,
)
