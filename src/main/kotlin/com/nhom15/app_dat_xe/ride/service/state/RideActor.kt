package com.nhom15.app_dat_xe.ride.service.state

import com.nhom15.app_dat_xe.common.enums.Role

/** Người hoặc tiến trình hệ thống thực hiện thay đổi trạng thái. */
data class RideActor(
    val userId: Long?,
    val role: Role?,
) {
    val isSystem: Boolean
        get() = userId == null && role == null
}

val SYSTEM_RIDE_ACTOR = RideActor(userId = null, role = null)

fun customerRideActor(userId: Long) = RideActor(userId, Role.CUSTOMER)

fun driverRideActor(driverId: Long) = RideActor(driverId, Role.DRIVER)

fun adminRideActor(userId: Long) = RideActor(userId, Role.ADMIN)
