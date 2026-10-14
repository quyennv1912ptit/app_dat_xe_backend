package com.nhom15.app_dat_xe.notification.service

import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.notification.dto.NotificationHistoryItem
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class NotificationHistoryService(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun list(actor: AuthUser): List<NotificationHistoryItem> {
        val recipientRole = when (actor.role) {
            Role.CUSTOMER -> "USER"
            Role.DRIVER -> "DRIVER"
            Role.ADMIN -> throw ForbiddenException(message = "Admin không có lịch sử thông báo cá nhân")
        }
        return jdbcTemplate.query(
            """SELECT message_id, payload_type, payload_json, created_at
               FROM notification_history WHERE recipient_role = ? AND recipient_id = ?
               ORDER BY created_at DESC, id DESC LIMIT 100""".trimIndent(),
            { row, _ ->
                NotificationHistoryItem(
                    messageId = row.getString("message_id"),
                    type = row.getString("payload_type"),
                    payload = objectMapper.readTree(row.getString("payload_json")),
                    createdAt = row.getTimestamp("created_at").toInstant(),
                )
            },
            recipientRole,
            actor.id,
        )
    }
}
