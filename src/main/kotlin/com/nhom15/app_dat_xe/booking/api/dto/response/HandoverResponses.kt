package com.nhom15.app_dat_xe.booking.api.dto.response

import com.nhom15.app_dat_xe.booking.entity.HandoverPhase
import com.nhom15.app_dat_xe.booking.entity.HandoverPhotoAngle
import java.time.Instant

data class HandoverPhotoResponse(
    val id: Long,
    val angle: HandoverPhotoAngle,
    val fileUrl: String,
    val createdAt: Instant,
)

data class HandoverReportResponse(
    val id: Long,
    val phase: HandoverPhase,
    val odometer: Long,
    val fuelLevel: Int?,
    val notes: String?,
    val confirmedByCustomer: Boolean,
    val photos: List<HandoverPhotoResponse>,
    val createdAt: Instant,
)
