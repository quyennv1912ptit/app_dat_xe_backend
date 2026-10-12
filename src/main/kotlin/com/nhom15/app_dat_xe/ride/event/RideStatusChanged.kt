package com.nhom15.app_dat_xe.ride.event

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.event.DomainEvent
import java.time.Instant
import java.util.UUID

/** Ride phát mỗi khi trạng thái đổi; Realtime và Payment có thể đăng ký nhận. */
data class RideStatusChanged(
    val rideId: Long,
    val customerId: Long,
    val driverId: Long?,
    /** null khi chuyến mới được tạo */
    val from: RideStatus?,
    val to: RideStatus,
    /** Người thực hiện (null nếu do hệ thống, ví dụ hết hạn tìm tài xế) */
    val actorId: Long? = null,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
