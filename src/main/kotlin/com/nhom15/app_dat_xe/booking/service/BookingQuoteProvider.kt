package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import java.math.BigDecimal
import java.time.Instant

data class BookingQuoteInput(
    val pickup: GeoPoint,
    val dropoff: GeoPoint,
    val pricingAt: Instant,
)

data class BookingQuote(
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

/** Tính giá theo tọa độ và thời điểm, dùng chung cho tạo booking và báo giá. */
fun interface BookingQuoteProvider {
    fun quote(input: BookingQuoteInput): BookingQuote
}
