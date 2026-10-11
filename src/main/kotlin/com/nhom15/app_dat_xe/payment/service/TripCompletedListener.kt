// Lắng nghe sự kiện TripCompleted để kiểm tra trạng thái thanh toán của chuyến đi.

package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.event.TripCompleted
import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import com.nhom15.app_dat_xe.payment.repository.PaymentRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TripCompletedListener(
    private val paymentRepository: PaymentRepository
) {

    @EventListener
    fun handleTripCompleted(event: TripCompleted) {

        val payment = paymentRepository
            .findByBookingId(event.bookingId)
            .orElse(null)

        if (payment == null) {
            println(
                "TripCompleted: chưa có Payment cho bookingId=${event.bookingId}"
            )
            return
        }

        if (payment.status == PaymentStatus.PAID) {
            println(
                "TripCompleted: Payment đã PAID, không thanh toán lại. " +
                "bookingId=${event.bookingId}"
            )
            return
        }

        if (payment.status == PaymentStatus.PENDING) {
            println(
                "TripCompleted: Payment đang PENDING. " +
                "bookingId=${event.bookingId}, " +
                "finalFare=${event.finalFare.amount}"
            )
            return
        }

        println(
            "TripCompleted: Payment có trạng thái ${payment.status}. " +
            "bookingId=${event.bookingId}"
        )
    }
}