package com.nhom15.app_dat_xe.booking.entity

import com.nhom15.app_dat_xe.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "vehicle_handover_reports",
    uniqueConstraints = [UniqueConstraint(name = "uk_handover_booking_phase", columnNames = ["booking_id", "phase"])],
)
class VehicleHandoverReport(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    var booking: Booking,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var phase: HandoverPhase,

    @Column(nullable = false)
    var odometer: Long,

    @Column(name = "fuel_level")
    var fuelLevel: Int? = null,

    @Column(columnDefinition = "text")
    var notes: String? = null,

    @Column(name = "confirmed_by_customer", nullable = false)
    var confirmedByCustomer: Boolean = false,
) : BaseEntity()
