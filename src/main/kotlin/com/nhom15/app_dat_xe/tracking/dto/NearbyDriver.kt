package com.nhom15.app_dat_xe.tracking.dto

import com.nhom15.app_dat_xe.common.domain.GeoPoint

data class NearbyDriver(
    val driverId: Long,
    val distanceKm: Double,
    val location: GeoPoint
)
