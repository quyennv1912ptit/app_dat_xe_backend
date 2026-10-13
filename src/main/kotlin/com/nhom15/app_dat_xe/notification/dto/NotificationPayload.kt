package com.nhom15.app_dat_xe.notification.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.nhom15.app_dat_xe.common.websocket.PayloadType

sealed interface NotificationPayload {
    @get:JsonIgnore
    val type: PayloadType
}