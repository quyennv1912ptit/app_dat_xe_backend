package com.nhom15.app_dat_xe.ride.event

import com.nhom15.app_dat_xe.common.event.DomainEvent
import java.time.Instant
import java.util.UUID

/** Matching báo "đã hết tài xế để mời". RideService nghe event này để ghi DB và báo khách. */
data class RideNoDriverFound(
    val rideId: Long,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent