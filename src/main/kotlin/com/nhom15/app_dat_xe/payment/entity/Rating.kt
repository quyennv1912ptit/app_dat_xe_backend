// Lưu thông tin đánh giá giữa khách hàng và tài xế sau khi chuyến đi hoàn thành.

package com.nhom15.app_dat_xe.payment.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["booking_id", "rater_id"]
        )
    ]
)
class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "booking_id", nullable = false)
    var bookingId: Long = 0

    @Column(name = "rater_id", nullable = false)
    var raterId: Long = 0

    @Column(name = "ratee_id", nullable = false)
    var rateeId: Long = 0

    @Column(name = "stars", nullable = false)
    var stars: Int = 0

    @Column(name = "tags")
    var tags: String? = null

    @Column(name = "comment", columnDefinition = "TEXT")
    var comment: String? = null

    @Column(name = "direction", nullable = false)
    var direction: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}