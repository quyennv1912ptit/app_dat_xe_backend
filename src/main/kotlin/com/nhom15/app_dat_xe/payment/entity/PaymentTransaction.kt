// Lưu thông tin chi tiết của từng giao dịch thanh toán qua cổng thanh toán.

package com.nhom15.app_dat_xe.payment.entity

import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "payment_transactions")
class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "payment_id", nullable = false)
    var paymentId: Long = 0

    @Column(name = "provider", nullable = false)
    var provider: String = ""

    @Column(name = "provider_transaction_id")
    var providerTransactionId: String? = null

    @Column(name = "amount", nullable = false)
    var amount: Long = 0

    @Column(name = "raw_response", columnDefinition = "TEXT")
    var rawResponse: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}