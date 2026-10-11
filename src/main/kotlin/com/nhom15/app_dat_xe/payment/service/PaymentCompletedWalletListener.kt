
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import com.nhom15.app_dat_xe.common.event.PaymentCompleted
import com.nhom15.app_dat_xe.payment.repository.DriverEarningRepository
import com.nhom15.app_dat_xe.payment.repository.PaymentRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentCompletedWalletListener(
    private val paymentRepository: PaymentRepository,
    private val driverEarningRepository: DriverEarningRepository
) {

    @EventListener
    @Transactional
    fun handlePaymentCompleted(event: PaymentCompleted) {
        val payment = paymentRepository.findById(event.paymentId)
            .orElse(null)
            ?: return

        // Chỉ ghi thu nhập khi payment đã được xác nhận PAID.
        if (payment.status != PaymentStatus.PAID) {
            return
        }

        // Đảm bảo sự kiện khớp với payment trong database.
        if (
            payment.bookingId != event.bookingId ||
            payment.customerId != event.customerId ||
            payment.driverId != event.driverId ||
            payment.amount != event.amount.amount
        ) {
            throw IllegalStateException(
                "PaymentCompleted không khớp payment id=${event.paymentId}"
            )
        }

        driverEarningRepository.recordEarning(event)
    }
}