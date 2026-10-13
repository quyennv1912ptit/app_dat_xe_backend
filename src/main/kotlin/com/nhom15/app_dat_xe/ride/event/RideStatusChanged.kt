package com.nhom15.app_dat_xe.ride.event

import com.nhom15.app_dat_xe.common.enums.ActorRole
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.event.DomainEvent
import java.time.Instant
import java.util.UUID

data class RideStatusChanged(
    val rideId: Long,
    val customerId: Long,
    val driverId: Long?,

    val from: RideStatus?,
    val to: RideStatus,

    val actor: ActorRole = ActorRole.SYSTEM,

    val reason: String? = null,

    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
