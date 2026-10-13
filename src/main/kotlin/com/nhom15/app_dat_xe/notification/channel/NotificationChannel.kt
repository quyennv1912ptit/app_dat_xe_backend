package com.nhom15.app_dat_xe.notification.channel

import com.nhom15.app_dat_xe.common.websocket.WsEnvelope
import com.nhom15.app_dat_xe.notification.api.Recipient

interface NotificationChannel {
    fun isAvailable(to: Recipient): Boolean
    fun send(to: Recipient, envelope: WsEnvelope<*>): Boolean
}