package com.nhom15.app_dat_xe.booking.api.dto.response

import com.nhom15.app_dat_xe.booking.entity.BookingType
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import java.math.BigDecimal
import java.time.Instant

data class EstimateBookingResponse(
    val pricingRuleId: Long,
    val distanceKm: BigDecimal,
    val estimatedDurationMinutes: Long,
    val baseFare: Long,
    val distanceFare: Long,
    val waitingFare: Long,
    val nightSurcharge: Long,
    val returnFee: Long,
    val estimatedFare: Long,
)

data class CreateBookingResponse(
    val id: Long,
    val code: String,
    val status: BookingStatus,
    val estimatedFare: Long,
    val createdAt: Instant,
)

data class BookingSummaryResponse(
    val id: Long,
    val code: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val type: BookingType,
    val status: BookingStatus,
    val estimatedFare: Long,
    val finalFare: Long?,
    val scheduledAt: Instant?,
    val createdAt: Instant,
)

data class GeoPointResponse(
    val lat: Double,
    val lng: Double,
)

data class BookingDetailResponse(
    val id: Long,
    val code: String,
    val customerId: Long,
    val driverId: Long?,
    val vehicleId: Long,
    val type: BookingType,
    val scheduledAt: Instant?,
    val pickupAddress: String,
    val pickup: GeoPointResponse,
    val dropoffAddress: String,
    val dropoff: GeoPointResponse,
    val distanceKm: BigDecimal?,
    val estimatedFare: Long,
    val finalFare: Long?,
    val status: BookingStatus,
    val cancelReason: String?,
    val cancelledBy: Long?,
    val statusHistory: List<BookingStatusHistoryResponse>,
    val handoverReports: List<HandoverReportResponse>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class BookingStatusHistoryResponse(
    val id: Long,
    val fromStatus: BookingStatus?,
    val toStatus: BookingStatus,
    val actorId: Long?,
    val note: String?,
    val createdAt: Instant,
)

data class BookingStatusResponse(
    val bookingId: Long,
    val code: String,
    val previousStatus: BookingStatus,
    val currentStatus: BookingStatus,
    val changedAt: Instant,
)
