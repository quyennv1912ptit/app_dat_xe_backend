package com.nhom15.app_dat_xe.account.entity

import com.nhom15.app_dat_xe.common.entity.BaseEntity
import com.nhom15.app_dat_xe.common.enums.UserStatus
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "users")
class Users : BaseEntity() {

    @Column(name = "firebase_uid", unique = true, nullable = false)
    var firebaseUid: String? = null

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String? = null

    @Column(name = "email")
    var email: String? = null

    @Column(name = "full_name")
    var fullName: String? = null

    @Column(name = "avatar_url") // Lưu URL từ Cloudinary
    var avatarUrl: String? = null

    @Column(name = "average_rating")
    var averageRating: Double = 5.0

    @Column(name = "default_payment_method")
    var defaultPaymentMethod: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.ACTIVE

    @Column(name = "wallet_balance", precision = 38, scale = 2)
    var walletBalance: BigDecimal = BigDecimal.ZERO
}