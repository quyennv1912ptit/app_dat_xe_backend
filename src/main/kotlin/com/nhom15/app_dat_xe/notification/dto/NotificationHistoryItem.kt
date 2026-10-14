package com.nhom15.app_dat_xe.notification.dto

import tools.jackson.databind.JsonNode
import java.time.Instant

data class NotificationHistoryItem(
    val messageId: String,
    val type: String,
    val payload: JsonNode,
    val createdAt: Instant,
)
