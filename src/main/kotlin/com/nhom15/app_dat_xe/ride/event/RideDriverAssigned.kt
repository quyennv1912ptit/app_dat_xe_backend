package com.nhom15.app_dat_xe.ride.event

import com.nhom15.app_dat_xe.common.event.DomainEvent
import java.time.Instant
import java.util.UUID

/** Phát bởi matching khi có tài xế nhận chuyến thành công. Realtime nghe để báo khách. */
data class RideDriverAssigned(
    val rideId: Long,
    val customerId: Long,
    val driverId: Long,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
