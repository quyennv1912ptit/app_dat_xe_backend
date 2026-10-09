package com.nhom15.app_dat_xe.booking.api.dto.request

import com.nhom15.app_dat_xe.booking.entity.BookingType
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.Instant

data class EstimateBookingRequest(
    @field:Positive
    val vehicleId: Long,

    @field:NotBlank
    @field:Size(max = 500)
    val pickupAddress: String,

    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val pickupLat: Double,

    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val pickupLng: Double,

    @field:NotBlank
    @field:Size(max = 500)
    val dropoffAddress: String,

    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val dropoffLat: Double,

    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val dropoffLng: Double,

    @field:Future
    val scheduledAt: Instant? = null,
)

data class CreateBookingRequest(
    @field:Positive
    val vehicleId: Long,

    val type: BookingType = BookingType.INSTANT,

    @field:Future
    val scheduledAt: Instant? = null,

    @field:NotBlank
    @field:Size(max = 500)
    val pickupAddress: String,

    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val pickupLat: Double,

    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val pickupLng: Double,

    @field:NotBlank
    @field:Size(max = 500)
    val dropoffAddress: String,

    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    val dropoffLat: Double,

    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    val dropoffLng: Double,
)

data class BookingListFilter(
    val status: BookingStatus? = null,

    @field:PositiveOrZero
    val page: Int = 0,

    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)

data class CancelBookingRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val reason: String,
)

data class DriverStartArrivingRequest(
    @field:Size(max = 1_000)
    val note: String? = null,
)

data class DriverArrivedRequest(
    @field:Size(max = 1_000)
    val note: String? = null,
)

data class StartTripRequest(
    @field:Size(max = 1_000)
    val note: String? = null,
)

data class ArriveDestinationRequest(
    @field:Size(max = 1_000)
    val note: String? = null,
)

data class CompleteBookingRequest(
    @field:Size(max = 1_000)
    val note: String? = null,
)
