package com.nhom15.app_dat_xe.tracking.dto

import com.nhom15.app_dat_xe.common.domain.GeoPoint

data class Route(
    val distance: Double,
    val duration: Double,
    val detailedJson: String,
    val coordinates: List<GeoPoint> = emptyList(),
)
