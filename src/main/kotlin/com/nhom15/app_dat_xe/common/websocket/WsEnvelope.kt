package com.nhom15.app_dat_xe.common.websocket

import java.util.UUID

/**
 * Vỏ bọc chung cho mọi tin gửi tới app, dù đi qua WebSocket hay FCM.
 *
 * - [type] cho app biết payload thuộc loại nào để parse.
 * - [messageId] để app chống xử lý trùng khi cùng một tin tới cả WS lẫn FCM.
 * - [serverTime] (epoch ms) để app tính thời gian còn lại theo đồng hồ server, không phụ thuộc giờ máy.
 * - [version] tăng khi đổi schema không tương thích.
 *
 * T không ràng buộc để `common` không phải phụ thuộc ngược vào `notification`.
 */
data class WsEnvelope<T>(
    val type: PayloadType,
    val payload: T,
    val messageId: String = UUID.randomUUID().toString(),
    val serverTime: Long = System.currentTimeMillis(),
    val version: Int = 1
)