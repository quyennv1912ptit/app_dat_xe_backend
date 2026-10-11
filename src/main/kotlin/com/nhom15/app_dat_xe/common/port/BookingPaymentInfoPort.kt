// Cung cấp thông tin thanh toán của Booking cho module Payment.

package com.nhom15.app_dat_xe.common.port

import com.nhom15.app_dat_xe.common.domain.Money

data class BookingPaymentInfo(
    val bookingId: Long,
    val customerId: Long,
    val driverId: Long?,
    val estimatedFare: Money,
    val distanceKm: Double,
    val voucherCode: String?
)

interface BookingPaymentInfoPort {
    fun getPaymentInfo(bookingId: Long): BookingPaymentInfo
}