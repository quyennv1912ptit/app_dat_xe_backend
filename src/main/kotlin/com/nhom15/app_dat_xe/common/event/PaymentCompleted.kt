package com.nhom15.app_dat_xe.common.event

import com.nhom15.app_dat_xe.common.domain.Money
import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import java.time.Instant
import java.util.UUID

/** Phát bởi payment khi thanh toán thành công. Booking-core và realtime nghe. */
data class PaymentCompleted(
    val bookingId: Long,
    val paymentId: Long,
    val customerId: Long,
    val driverId: Long,
    val amount: Money,
    val method: PaymentMethod,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
