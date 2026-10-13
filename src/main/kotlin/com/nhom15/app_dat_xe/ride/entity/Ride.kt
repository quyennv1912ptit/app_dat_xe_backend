package com.nhom15.app_dat_xe.ride.entity

import com.nhom15.app_dat_xe.common.entity.BaseEntity
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.domain.Location
import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.enums.VehicleType
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.ConflictException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.Index
import jakarta.persistence.Transient
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "rides", indexes = [
    Index(name = "idx_rides_customer_created", columnList = "customer_id,created_at"),
    Index(name = "idx_rides_customer_status", columnList = "customer_id,status"),
    Index(name = "idx_rides_driver_status", columnList = "driver_id,status"),
    Index(name = "idx_rides_status_scheduled", columnList = "status,scheduled_at"),
    Index(name = "idx_rides_vehicle", columnList = "vehicle_id"),
])
class Ride(
    @Column(name = "customer_id", nullable = false)
    var customerId: Long,

    @Column(name = "driver_id")
    var driverId: Long? = null,

    @Column(name = "vehicle_id", nullable = false)
    var vehicleId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var type: RideType = RideType.INSTANT,

    @Column(name = "scheduled_at")
    var scheduledAt: Instant? = null,

    @Column(name = "pickup_address", length = 255)
    var pickupAddress: String?,

    pickup: GeoPoint?,

    @Column(name = "dropoff_address", length = 255)
    var dropoffAddress: String?,

    dropoff: GeoPoint?,

    distanceKm: BigDecimal? = null,

    @Column(name = "fare_estimate", nullable = false)
    var estimatedFare: Long,

    @Column(name = "final_fare")
    var finalFare: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: RideStatus = RideStatus.PENDING,

    @Column(name = "cancel_reason", length = 255)
    var cancelReason: String? = null,

    @Column(name = "cancelled_by")
    var cancelledBy: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    var vehicleType: VehicleType = VehicleType.CAR,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var transmission: Transmission = Transmission.AUTO,

    @Column(name = "license_plate", length = 20)
    var licensePlate: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    var paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @Column(length = 500)
    var note: String? = null,

    @Column(name = "accepted_at")
    var acceptedAt: Instant? = null,

    @Column(name = "started_at")
    var startedAt: Instant? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: Instant? = null,

    @Column(name = "discount_amount")
    var discountAmount: Long? = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by_role", length = 20)
    var cancelledByRole: RideActorRole? = null,
) : BaseEntity() {
    @Column(name = "pickup_lat")
    var pickupLat: Double? = pickup?.lat

    @Column(name = "pickup_lng")
    var pickupLng: Double? = pickup?.lng

    @Column(name = "dropoff_lat")
    var dropoffLat: Double? = dropoff?.lat

    @Column(name = "dropoff_lng")
    var dropoffLng: Double? = dropoff?.lng

    @Column(name = "distance_km")
    var distanceKm: Double? = distanceKm?.toDouble()

    // Preserve the existing API/Matching accessors without adding extra database columns.
    @get:Transient
    var distanceKmDecimal: BigDecimal?
        get() = distanceKm?.let(BigDecimal::valueOf)
        set(value) { distanceKm = value?.toDouble() }

    @get:Transient
    val pickupPoint: GeoPoint get() = GeoPoint(pickupLat ?: missingLocation(), pickupLng ?: missingLocation())

    @get:Transient
    val dropoffPoint: GeoPoint get() = GeoPoint(dropoffLat ?: missingLocation(), dropoffLng ?: missingLocation())

    @get:Transient
    val pickup: Location get() = Location(pickupPoint, pickupAddress ?: missingLocation())

    @get:Transient
    val dropoff: Location get() = Location(dropoffPoint, dropoffAddress ?: missingLocation())

    @get:Transient
    val fareEstimate: Long get() = estimatedFare

    @Version
    @Column(nullable = false)
    var version: Long = 0

    private fun missingLocation(): Nothing =
        throw ConflictException(ErrorCode.INVALID_RIDE_STATE, "Chuyến thiếu thông tin địa điểm")
}
