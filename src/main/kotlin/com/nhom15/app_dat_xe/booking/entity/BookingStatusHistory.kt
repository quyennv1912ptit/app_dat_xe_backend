package com.nhom15.app_dat_xe.booking.entity

import com.nhom15.app_dat_xe.common.domain.BaseEntity
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "booking_status_histories")
class BookingStatusHistory(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    var booking: Booking,

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    var fromStatus: BookingStatus? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    var toStatus: BookingStatus,

    @Column(name = "actor_id")
    var actorId: Long? = null,

    @Column(columnDefinition = "text")
    var note: String? = null,
) : BaseEntity()
