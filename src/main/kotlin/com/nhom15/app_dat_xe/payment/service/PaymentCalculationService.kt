// Tính toán số tiền cần thanh toán dựa trên thông tin của Booking.

package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.port.BookingPaymentInfo
import org.springframework.stereotype.Service

@Service
class PaymentCalculationService {

    fun calculateAmount(bookingInfo: BookingPaymentInfo): Long {

        return bookingInfo.estimatedFare.amount
    }
}