package com.nhom15.app_dat_xe.payment.entity

import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "payments",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["booking_id"])
    ]
)
class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "booking_id", nullable = false)
    var bookingId: Long = 0

    @Column(name = "customer_id", nullable = false)
    var customerId: Long = 0

    @Column(name = "driver_id")
    var driverId: Long? = null

    @Column(name = "amount", nullable = false)
    var amount: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    var method: PaymentMethod = PaymentMethod.CASH

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING

    @Column(name = "paid_at")
    var paidAt: Instant? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
}