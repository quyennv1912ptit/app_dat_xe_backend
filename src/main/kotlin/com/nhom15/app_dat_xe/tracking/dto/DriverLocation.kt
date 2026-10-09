package com.nhom15.app_dat_xe.tracking.dto

import com.nhom15.app_dat_xe.common.domain.GeoPoint

data class DriverLocation(
    val driverId: Long,
    val location: GeoPoint
)