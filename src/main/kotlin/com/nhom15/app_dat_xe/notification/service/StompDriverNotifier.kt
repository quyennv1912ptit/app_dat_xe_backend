package com.nhom15.app_dat_xe.notification.service

import com.nhom15.app_dat_xe.common.websocket.PayloadType
import com.nhom15.app_dat_xe.common.websocket.WsEnvelope
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class StompDriverNotifier (
    private val messaging: SimpMessagingTemplate,
) {
    fun <T: Any> sendToDriver(driverId: Long, type: PayloadType, payload: T) {
        messaging.convertAndSendToUser(driverId.toString(), "/queue/offers", WsEnvelope(type, payload))
    }

    fun sendOfferExpired(driverId: Long, rideId: Long) {
        sendToDriver(driverId, PayloadType.OFFER_EXPIRED, mapOf("rideId" to rideId))
    }

    fun sendOfferCancelled(driverId: Long, rideId: Long) {
        sendToDriver(driverId, PayloadType.OFFER_CANCELLED, mapOf("rideId" to rideId))
    }
}