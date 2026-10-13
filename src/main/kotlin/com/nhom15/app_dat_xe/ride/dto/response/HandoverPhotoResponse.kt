package com.nhom15.app_dat_xe.ride.dto.response

import com.nhom15.app_dat_xe.ride.entity.HandoverPhotoAngle
import java.time.Instant

data class HandoverPhotoResponse(
    val id: Long,
    val angle: HandoverPhotoAngle,
    val fileUrl: String,
    val createdAt: Instant,
)
