package com.nhom15.app_dat_xe.matching.entity

import java.time.Instant

data class BookingOffer(
    val id: Long,
    val bookingId: Long,
    val driverId: Long,
    var status: OfferStatus = OfferStatus.PENDING,
    val round: Int,
    val distanceToPickupKm: Double,
    val sentAt: Instant,
    val expiresAt: Instant
) {
    enum class OfferStatus {
        PENDING, ACCEPTED, REJECTED, EXPIRED, CANCELLED
    }
}