package com.nhom15.app_dat_xe.common.websocket

import com.nhom15.app_dat_xe.common.websocket.DeliveryPolicy.PUSH_ALSO
import com.nhom15.app_dat_xe.common.websocket.DeliveryPolicy.WS_ONLY
import com.nhom15.app_dat_xe.common.websocket.DeliveryPolicy.WS_THEN_PUSH

enum class DeliveryPolicy {
    WS_ONLY,
    WS_THEN_PUSH,
    PUSH_ALSO
}

enum class PayloadType(val policy: DeliveryPolicy) {
    // Tài xế
    RIDE_OFFER(WS_THEN_PUSH),
    OFFER_EXPIRED(WS_ONLY),
    OFFER_CANCELLED(WS_ONLY),

    // Khách
    RIDE_ASSIGNED(WS_THEN_PUSH),
    RIDE_NO_DRIVER(PUSH_ALSO),

    // Cả hai bên
    RIDE_UPDATED(WS_ONLY),
    RIDE_CANCELLED(PUSH_ALSO),
    RIDE_COMPLETED(PUSH_ALSO),

}