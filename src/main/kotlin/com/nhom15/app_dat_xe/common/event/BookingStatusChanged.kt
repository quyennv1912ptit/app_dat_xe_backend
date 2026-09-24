package com.nhom15.app_dat_xe.common.event

import com.nhom15.app_dat_xe.common.enums.BookingStatus
import java.time.Instant
import java.util.UUID

/** Phát bởi booking-core mỗi khi status đổi. Realtime nghe để đẩy WebSocket, payment nghe khi cần. */
data class BookingStatusChanged(
    val bookingId: Long,
    val customerId: Long,
    val driverId: Long?,
    /** null khi booking mới được tạo */
    val from: BookingStatus?,
    val to: BookingStatus,
    /** Người thực hiện (null nếu do hệ thống, ví dụ hết hạn tìm tài xế) */
    val actorId: Long? = null,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
