package com.nhom15.app_dat_xe.booking.entity

import com.nhom15.app_dat_xe.common.domain.BaseEntity
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "bookings")
class Booking(
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
    var type: BookingType = BookingType.INSTANT,

    @Column(name = "scheduled_at")
    var scheduledAt: Instant? = null,

    @Column(name = "pickup_address", nullable = false, columnDefinition = "text")
    var pickupAddress: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lat", column = Column(name = "pickup_lat", nullable = false)),
        AttributeOverride(name = "lng", column = Column(name = "pickup_lng", nullable = false)),
    )
    var pickup: GeoPoint,

    @Column(name = "dropoff_address", nullable = false, columnDefinition = "text")
    var dropoffAddress: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lat", column = Column(name = "dropoff_lat", nullable = false)),
        AttributeOverride(name = "lng", column = Column(name = "dropoff_lng", nullable = false)),
    )
    var dropoff: GeoPoint,

    @Column(name = "distance_km", precision = 10, scale = 3)
    var distanceKm: BigDecimal? = null,

    @Column(name = "estimated_fare", nullable = false)
    var estimatedFare: Long,

    @Column(name = "final_fare")
    var finalFare: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: BookingStatus = BookingStatus.PENDING,

    @Column(name = "cancel_reason", columnDefinition = "text")
    var cancelReason: String? = null,

    @Column(name = "cancelled_by")
    var cancelledBy: Long? = null,
) : BaseEntity() {
    @Version
    var version: Long = 0
}
