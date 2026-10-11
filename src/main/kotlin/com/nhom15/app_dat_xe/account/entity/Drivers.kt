package com.nhom15.app_dat_xe.account.entity

import com.nhom15.app_dat_xe.common.entity.BaseEntity
import com.nhom15.app_dat_xe.common.enums.DriverStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "drivers")
class Drivers : BaseEntity() {

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

    @Column(name = "license_number")
    var licenseNumber: String? = null

    @Column(name = "license_class", length = 10)
    var licenseClass: String? = null

    @Column(name = "years_of_experience")
    var yearsOfExperience: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: DriverStatus = DriverStatus.OFFLINE

    @Column(name = "last_location_update")
    var lastLocationUpdate: LocalDateTime? = null

    @Column(name = "average_rating")
    var averageRating: Double = 5.0

    @Column(name = "bank_account_number")
    var bankAccountNumber: String? = null

    @Column(name = "wallet_balance", precision = 15, scale = 2)
    var walletBalance: BigDecimal = BigDecimal.ZERO

    @Column(name = "pending_payout", precision = 15, scale = 2)
    var pendingPayout: BigDecimal = BigDecimal.ZERO
}