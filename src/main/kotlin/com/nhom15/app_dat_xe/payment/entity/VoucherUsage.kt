package com.nhom15.app_dat_xe.payment.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "voucher_usages",
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["voucher_id", "user_id"]
        )
    ]
)
class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "voucher_id", nullable = false)
    var voucherId: Long = 0

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0

    @Column(name = "booking_id")
    var bookingId: Long? = null

    @Column(name = "used_at", nullable = false)
    var usedAt: Instant = Instant.now()
}