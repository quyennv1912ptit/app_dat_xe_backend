package com.nhom15.app_dat_xe.auth.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String? = null

    @Column(name = "email")
    var email: String? = null

    @Column(name = "password_hash")
    var passwordHash: String? = null

    @Column(name = "full_name")
    var fullName: String? = null

    @Column(name = "firebase_uid", unique = true)
    var firebaseUid: String? = null

    @Column(name = "avatar_url")
    var avatarUrl: String? = null

    @Column(name = "gender")
    var gender: String? = null

    @Column(name = "date_of_birth")
    var birth: LocalDate? = null

    @Column(name = "status")
    var status: String? = null

    @Column(name = "is_phone_verified")
    var phoneVerified: Boolean? = null

    @Column(name = "is_email_verified")
    var emailVerified: Boolean? = null

    @Column(name = "average_rating")
    var averageRating: Double? = null

    @Column(name = "total_trips")
    var totalTrips: Int = 0

    @Column(name = "total_cancelled")
    var totalCancelled: Int = 0

    @Column(name = "cancel_rate_30d")
    var cancelRate30d: Double? = null

    @Column(name = "default_payment_method")
    var defaultPaymentMethod: String? = null

    @Column(name = "wallet_balance")
    var walletBalance: BigDecimal? = null

    @Column(name = "device_id")
    var deviceId: String? = null

    @Column(name = "risk_score")
    var riskScore: Double? = null

    @Column(name = "is_flagged")
    var isFlagged: Boolean? = null

    @Column(name = "referral_code")
    var referralCode: String? = null

    @Column(name = "referred_by")
    var referredBy: Long? = null

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null
}