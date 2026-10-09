package com.nhom15.app_dat_xe.booking.entity

data class BookingTransitionCommand(
    val bookingId: Long,
    val event: BookingTransitionEvent,
    val actor: BookingActor,
    val note: String? = null,
    val cancelReason: String? = null,
)
