package com.nhom15.app_dat_xe.notification.dto

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.websocket.PayloadType

data class RideUpdatedPayload(
    val rideId: Long,
    val status: RideStatus
) : NotificationPayload {
    override val type: PayloadType get() = PayloadType.RIDE_UPDATED
}

data class HandoverConfirmedPayload(
    val rideId: Long,
    val confirmedByCustomer: Boolean = true,
) : NotificationPayload {
    override val type: PayloadType get() = PayloadType.HANDOVER_CONFIRMED
}

data class RideCancelledPayload(
    val rideId: Long,
    val reason: String?
) : NotificationPayload {
    override val type: PayloadType get() = PayloadType.RIDE_CANCELLED
}

data class RideCompletedPayload(
    val rideId: Long,
    val finalFare: Long,
    val distanceKm: Double?
) : NotificationPayload {
    override val type: PayloadType get() = PayloadType.RIDE_COMPLETED
}

data class RideNoDriverPayload(
    val rideId: Long,
    val status: RideStatus
) : NotificationPayload {
    override val type: PayloadType get() = PayloadType.RIDE_NO_DRIVER
}
