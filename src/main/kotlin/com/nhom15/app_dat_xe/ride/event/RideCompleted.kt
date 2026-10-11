package com.nhom15.app_dat_xe.ride.event

import com.nhom15.app_dat_xe.common.domain.Money
import com.nhom15.app_dat_xe.common.event.DomainEvent
import java.time.Instant
import java.util.UUID

/** Phát bởi booking-core khi chuyến kết thúc. Payment nghe để chốt tiền, trừ hoa hồng. */
data class RideCompleted(
    val rideId: Long,
    val customerId: Long,
    val driverId: Long,
    val finalFare: Money,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
