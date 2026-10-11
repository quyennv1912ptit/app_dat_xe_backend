
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import com.nhom15.app_dat_xe.payment.event.RidePaymentCompleted
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
    fun handlePaymentCompleted(event: RidePaymentCompleted) {
        val payment = paymentRepository.findById(event.paymentId)
            .orElse(null) ?: return

        if (payment.status != PaymentStatus.PAID) {
            return
        }

        if (
            payment.bookingId != event.rideId ||
            payment.customerId != event.customerId ||
            payment.driverId != event.driverId ||
            payment.amount != event.amount.amount
        ) {
            throw IllegalStateException(
                "RidePaymentCompleted không khớp payment id=${event.paymentId}"
            )
        }

        driverEarningRepository.recordEarning(event)
    }
}