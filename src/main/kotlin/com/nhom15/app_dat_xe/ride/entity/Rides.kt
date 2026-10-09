package com.nhom15.app_dat_xe.ride.entity

import com.nhom15.app_dat_xe.common.domain.Location
import com.nhom15.app_dat_xe.common.entity.BaseEntity
import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.enums.VehicleType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "rides",
    indexes = [
        Index(name = "idx_rides_customer_status", columnList = "customer_id, status"),
        Index(name = "idx_rides_driver_status", columnList = "driver_id, status"),
        Index(name = "idx_rides_status_scheduled", columnList = "status, scheduled_at")
    ]
)
class Rides(
    @Column(name = "customer_id", nullable = false)
    val customerId: Long,

    @Column(name = "driver_id")
    var driverId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: RideStatus = RideStatus.SEARCHING,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lat", column = Column(name = "pickup_lat", nullable = false)),
        AttributeOverride(name = "lng", column = Column(name = "pickup_lng", nullable = false)),
        AttributeOverride(name = "address", column = Column(name = "pickup_address", nullable = false))
    )
    val pickup: Location,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "lat", column = Column(name = "dropoff_lat", nullable = false)),
        AttributeOverride(name = "lng", column = Column(name = "dropoff_lng", nullable = false)),
        AttributeOverride(name = "address", column = Column(name = "dropoff_address", nullable = false))
    )
    val dropoff: Location,

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    val vehicleType: VehicleType,

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, length = 30)
    val transmission: Transmission,

    @Column(name = "license_plate", length = 20)
    val licensePlate: String? = null,

    @Column(name = "fare_estimate", nullable = false)
    val fareEstimate: Long,

    @Column(name = "final_fare")
    var finalFare: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    val paymentMethod: PaymentMethod,

    @Column(name = "note", length = 500)
    val note: String? = null,

    @Column(name = "scheduled_at")
    val scheduledAt: Instant? = null,

    @Column(name = "distance_km")
    var distanceKm: Double? = null,

    @Column(name = "accepted_at")
    var acceptedAt: Instant? = null,

    @Column(name = "started_at")
    var startedAt: Instant? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: Instant? = null,

    @Column(name = "cancel_reason")
    var cancelReason: String? = null
) : BaseEntity() {

    /** Khoá lạc quan: hai luồng cùng sửa một chuyến (nhận và huỷ) thì bên ghi sau bị từ chối thay vì ghi đè. */
    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
        protected set
}