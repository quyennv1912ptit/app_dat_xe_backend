package com.nhom15.app_dat_xe.payment.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "vouchers",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["code"])
    ]
)
class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "code", nullable = false, unique = true)
    var code: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    var discountType: DiscountType = DiscountType.PERCENT

    // PERCENT: 20 = 20%
    // FIXED: 50000 = giảm 50.000đ
    @Column(name = "value", nullable = false)
    var value: Long = 0

    // Số tiền giảm tối đa.
    // Chỉ thực sự có ý nghĩa với PERCENT.
    @Column(name = "max_discount")
    var maxDiscount: Long? = null

    // Đơn hàng tối thiểu để được sử dụng voucher.
    @Column(name = "minimum_order_amount", nullable = false)
    var minimumOrderAmount: Long = 0

    @Column(name = "valid_from", nullable = false)
    var validFrom: Instant = Instant.now()

    @Column(name = "valid_to", nullable = false)
    var validTo: Instant = Instant.now()

    // Tổng số lượt được sử dụng trên toàn hệ thống.
    @Column(name = "usage_limit")
    var usageLimit: Int? = null

    // Tổng số lượt đã sử dụng.
    @Column(name = "usage_count", nullable = false)
    var usageCount: Int = 0
}