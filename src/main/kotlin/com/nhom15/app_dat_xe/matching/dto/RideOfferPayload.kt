package com.nhom15.app_dat_xe.matching.dto

import com.nhom15.app_dat_xe.common.domain.Location
import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.Transmission

data class RideOfferPayload(
    val rideId: String,

    val expiresAt: Long,
    val pickup: Location,
    val destination: Location,

    val distanceToPickupKm: Double,
    val etaToPickupMin: Int,

    val tripDistanceKm: Double,
    val estDurationMin: Int,

    val fareEstimate: Long,
    val driverEarning: Long,

    val paymentMethod: PaymentMethod,

    val vehicle: VehicleInfo,
    val customer: CustomerBrief,
    val note: String?
)

data class VehicleInfo(val type: String, val transmission: Transmission)
data class CustomerBrief(val displayName: String, val rating: Double)
