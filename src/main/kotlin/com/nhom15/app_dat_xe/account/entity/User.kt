package com.nhom15.app_dat_xe.account.entity

import com.nhom15.app_dat_xe.common.entity.BaseEntity
import com.nhom15.app_dat_xe.common.enums.UserStatus
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "users")
class User : BaseEntity() {

    @Column(name = "firebase_uid", unique = true, nullable = false)
    var firebaseUid: String? = null

    @Column(name = "phone_number", unique = true, nullable = false)
    var phoneNumber: String? = null

    @Column(name = "email")
    var email: String? = null

    @Column(name = "full_name")
    var fullName: String? = null

    @Column(name = "avatar_url")
    var avatarUrl: String? = null

    @Column(name = "average_rating")
    var averageRating: Double? = null

    @Column(name = "default_payment_method")
    var defaultPaymentMethod: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.ACTIVE

    @Column(name = "wallet_balance")
    var walletBalance: Long = 0

    @Column(name = "cancel_rate_30d")
    var cancelRate30d: Double = 0.0

    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null

    @Column(name = "gender")
    var gender: String? = null

    @Column(name = "is_flagged")
    var isFlagged: Boolean = false

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null

    @Column(name = "total_cancelled")
    var totalCancelled: Int = 0

    @Column(name = "total_trips")
    var totalTrips: Int = 0
}