package com.nhom15.app_dat_xe.notification.dto

import com.nhom15.app_dat_xe.common.websocket.PayloadType

data class DriverBrief(
    val id: Long,
    val name: String? = null,
    val phone: String? = null,
    val rating: Double,
    val avatarUrl: String? = null
)

data class RideAssignedPayload(
    val rideId: Long,
    val driver: DriverBrief
) : NotificationPayload {
    override val type: PayloadType get() = PayloadType.RIDE_ASSIGNED
}