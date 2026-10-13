package com.nhom15.app_dat_xe.ride.dto.response

import com.nhom15.app_dat_xe.ride.entity.HandoverPhase
import java.time.Instant

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
