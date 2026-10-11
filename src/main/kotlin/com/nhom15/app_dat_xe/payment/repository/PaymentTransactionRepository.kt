// Cung cấp các thao tác truy vấn và lưu lịch sử giao dịch thanh toán.

package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.entity.PaymentTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentTransactionRepository : JpaRepository<PaymentTransaction, Long> {

    fun findByPaymentId(paymentId: Long): List<PaymentTransaction>

    fun findByProviderTransactionId(
        providerTransactionId: String
    ): PaymentTransaction?
}