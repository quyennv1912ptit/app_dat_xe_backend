package com.nhom15.app_dat_xe.common.event

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.enums.Transmission
import java.time.Instant
import java.util.UUID

/** Phát bởi booking-core khi khách tạo booking xong (status = SEARCHING). Matching nghe để tìm tài xế. */
data class BookingCreated(
    val bookingId: Long,
    val customerId: Long,
    val pickup: GeoPoint,
    val transmission: Transmission,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
