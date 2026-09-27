package com.nhom15.app_dat_xe.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "drivers")
class Drivers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String? = null

    @Column(name = "firebase_uid", unique = true)
    var firebaseUid: String? = null

    @Column(name = "email")
    var email: String? = null

    @Column(name = "full_name")
    var fullName: String? = null

    @Column(name = "avatar_url")
    var avatarUrl: String? = null

    @Column(name = "national_id")
    var nationalId: String? = null

    @Column(name = "license_number")
    var licenseNumber: String? = null

    @Column(name = "license_expiry_date")
    var licenseExpiryDate: LocalDate? = null

    @Column(name = "license_verified")
    var licenseVerified: Boolean? = null

    @Column(name = "current_vehicle_id")
    var currentVehicleId: Long? = null

    @Column(name = "status")
    var status: String? = null

    @Column(name = "online_status")
    var onlineStatus: String? = null

    @Column(name = "current_lat")
    var currentLat: Double? = null

    @Column(name = "current_lng")
    var currentLng: Double? = null

    @Column(name = "last_location_update")
    var lastLocationUpdate: LocalDateTime? = null

    @Column(name = "average_rating")
    var averageRating: Double? = null

    @Column(name = "total_trips")
    var totalTrips: Int = 0

    @Column(name = "completion_rate")
    var completionRate: Double? = null

    @Column(name = "cancel_rate_30d")
    var cancelRate30d: Double? = null

    @Column(name = "acceptance_rate")
    var acceptanceRate: Double? = null

    @Column(name = "bank_account_number")
    var bankAccountNumber: String? = null

    @Column(name = "wallet_balance", precision = 15, scale = 2)
    var walletBalance: BigDecimal? = null

    @Column(name = "pending_payout", precision = 15, scale = 2)
    var pendingPayout: BigDecimal? = null

    @Column(name = "device_id")
    var deviceId: String? = null

    @Column(name = "risk_score")
    var riskScore: Double? = null

    @Column(name = "is_flagged")
    var isFlagged: Boolean? = null

    @Column(name = "gps_spoofing_count")
    var gpsSpoofingCount: Int = 0

    @Column(name = "multi_account_flag")
    var multiAccountFlag: Boolean? = null

    @Column(name = "background_check_status")
    var backgroundCheckStatus: String? = null

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
}