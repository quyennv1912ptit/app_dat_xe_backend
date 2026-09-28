package com.nhom15.app_dat_xe.matching.entity

import java.time.Instant

data class DriverAvailability(
    val driverId: Long,
    var status: DriverStatus = DriverStatus.OFFLINE,
    var lastOnlineAt: Instant? = null,
    var currentBookingId: Long? = null
) {
    enum class DriverStatus {
        OFFLINE, ONLINE, BUSY
    }
}