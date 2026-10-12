package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.ride.dto.internal.DrivingRoute

/** Cung cấp quãng đường chạy xe thực tế để tính giá ride. */
fun interface DrivingRouteProvider {
    fun findRoute(pickup: GeoPoint, dropoff: GeoPoint): DrivingRoute
}
