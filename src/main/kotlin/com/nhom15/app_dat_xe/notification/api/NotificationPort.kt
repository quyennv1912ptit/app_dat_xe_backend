package com.nhom15.app_dat_xe.notification.api

import com.nhom15.app_dat_xe.notification.dto.NotificationPayload

interface NotificationPort {
    fun send(to: Recipient, payload: NotificationPayload): DeliveryResult
}

enum class DeliveryResult { SENT_WS, SENT_PUSH, UNREACHABLE }