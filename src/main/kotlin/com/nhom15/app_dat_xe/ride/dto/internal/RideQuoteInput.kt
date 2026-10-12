package com.nhom15.app_dat_xe.ride.dto.internal

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import java.time.Instant

data class RideQuoteInput(
    val pickup: GeoPoint,
    val dropoff: GeoPoint,
    val pricingAt: Instant,
)
