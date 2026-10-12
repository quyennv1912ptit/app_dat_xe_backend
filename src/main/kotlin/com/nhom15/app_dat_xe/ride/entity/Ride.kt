package com.nhom15.app_dat_xe.ride.entity

import com.nhom15.app_dat_xe.common.entity.BaseEntity
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.domain.Location
import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.enums.VehicleType
import com.nhom15.app_dat_xe.common.enums.RideStatus
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.Transient
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "bookings")
class Ride(
    @Column(nullable = false, unique = true, length = 32)
    var code: String,

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

    @Column(name = "pickup_address", nullable = false, columnDefinition = "text")
    var pickupAddress: String,

    pickup: GeoPoint,

    @Column(name = "dropoff_address", nullable = false, columnDefinition = "text")
    var dropoffAddress: String,

    dropoff: GeoPoint,

    distanceKm: BigDecimal? = null,

    @Column(name = "estimated_fare", nullable = false)
    var estimatedFare: Long,

    @Column(name = "final_fare")
    var finalFare: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: RideStatus = RideStatus.PENDING,

    @Column(name = "cancel_reason", columnDefinition = "text")
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
) : BaseEntity() {
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lat", column = Column(name = "pickup_lat", nullable = false)),
        AttributeOverride(name = "lng", column = Column(name = "pickup_lng", nullable = false)),
    )
    var pickupPoint: GeoPoint = pickup

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lat", column = Column(name = "dropoff_lat", nullable = false)),
        AttributeOverride(name = "lng", column = Column(name = "dropoff_lng", nullable = false)),
    )
    var dropoffPoint: GeoPoint = dropoff

    @Column(name = "distance_km", precision = 10, scale = 3)
    var distanceKmDecimal: BigDecimal? = distanceKm

    // Matching reads Location and Double; storage/API retain the legacy columns and decimal precision.
    @get:Transient
    val pickup: Location get() = Location(pickupPoint, pickupAddress)
    @get:Transient
    val dropoff: Location get() = Location(dropoffPoint, dropoffAddress)
    @get:Transient
    val distanceKm: Double? get() = distanceKmDecimal?.toDouble()
    @get:Transient
    val fareEstimate: Long get() = estimatedFare

    @Version
    var version: Long = 0
}
