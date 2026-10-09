package com.nhom15.app_dat_xe.common.websocket

import com.nhom15.app_dat_xe.common.domain.Location
import com.nhom15.app_dat_xe.common.enums.PaymentMethod
import com.nhom15.app_dat_xe.common.enums.Transmission
import java.util.UUID

data class WsEnvelope<T>(
    val type: PayloadType,
    val payload: T,
    val messageId: String = UUID.randomUUID().toString(),
    val serverTime: Long = System.currentTimeMillis()
)

enum class PayloadType {
    RIDE_OFFER,
    OFFER_EXPIRED,
    OFFER_CANCELLED,
    RIDE_ASSIGNED,
    RIDE_CANCELLED,
    RIDE_UPDATED
}

