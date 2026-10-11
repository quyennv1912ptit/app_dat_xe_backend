// Lắng nghe sự kiện BookingCreated để khởi tạo luồng xử lý thanh toán.

package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.event.BookingCreated
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BookingCreatedListener(
    private val paymentService: PaymentService
) {

    @EventListener
    fun handleBookingCreated(event: BookingCreated) {

        println(
            "Payment nhận BookingCreated: bookingId=${event.bookingId}, " +
            "customerId=${event.customerId}"
        )
    }
}