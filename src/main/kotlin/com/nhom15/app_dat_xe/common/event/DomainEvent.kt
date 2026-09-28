package com.nhom15.app_dat_xe.common.event

import java.time.Instant
import java.util.UUID

/** Interface cha của mọi event giữa các module. */
interface DomainEvent {
    val eventId: UUID
    val occurredAt: Instant
}
