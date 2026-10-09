package com.nhom15.app_dat_xe.booking.entity

import com.nhom15.app_dat_xe.common.enums.Role

/** Người hoặc tiến trình hệ thống thực hiện thay đổi trạng thái. */
data class BookingActor(
    val userId: Long?,
    val role: Role?,
) {
    val isSystem: Boolean
        get() = userId == null && role == null
}

val SYSTEM_BOOKING_ACTOR = BookingActor(userId = null, role = null)

fun customerBookingActor(userId: Long) = BookingActor(userId, Role.CUSTOMER)

fun driverBookingActor(driverId: Long) = BookingActor(driverId, Role.DRIVER)

fun adminBookingActor(userId: Long) = BookingActor(userId, Role.ADMIN)
