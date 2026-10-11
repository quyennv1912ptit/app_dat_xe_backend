
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import com.nhom15.app_dat_xe.payment.repository.PaymentRepository
import com.nhom15.app_dat_xe.ride.event.RideCompleted
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

// Lắng nghe sự kiện RideCompleted để kiểm tra trạng thái thanh toán.
@Component
class TripCompletedListener(
    private val paymentRepository: PaymentRepository
) {

    @EventListener
    fun handleRideCompleted(event: RideCompleted) {

        val payment = paymentRepository
            .findByBookingId(event.rideId)
            .orElse(null)

        if (payment == null) {
            println(
                "RideCompleted: chưa có Payment cho rideId=${event.rideId}"
            )
            return
        }

        if (payment.status == PaymentStatus.PAID) {
            println(
                "RideCompleted: Payment đã PAID, không thanh toán lại. " +
                    "rideId=${event.rideId}"
            )
            return
        }

        if (payment.status == PaymentStatus.PENDING) {
            println(
                "RideCompleted: Payment đang PENDING. " +
                    "rideId=${event.rideId}, " +
                    "finalFare=${event.finalFare.amount}"
            )
            return
        }

        println(
            "RideCompleted: Payment có trạng thái ${payment.status}. " +
                "rideId=${event.rideId}"
        )
    }
}