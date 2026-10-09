package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.common.domain.GeoPoint

data class DrivingRoute(
    val distanceMeters: Double,
    val durationSeconds: Double,
)

/** Cung cấp quãng đường chạy xe thực tế để tính giá booking. */
fun interface DrivingRouteProvider {
    fun findRoute(pickup: GeoPoint, dropoff: GeoPoint): DrivingRoute
}
