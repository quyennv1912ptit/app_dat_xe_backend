package com.nhom15.app_dat_xe.notification.service

import com.nhom15.app_dat_xe.common.websocket.WsEnvelope
import com.nhom15.app_dat_xe.common.websocket.DeliveryPolicy.*
import com.nhom15.app_dat_xe.notification.api.DeliveryResult
import com.nhom15.app_dat_xe.notification.api.DeliveryResult.*
import com.nhom15.app_dat_xe.notification.api.NotificationPort
import com.nhom15.app_dat_xe.notification.api.Recipient
import com.nhom15.app_dat_xe.notification.channel.FcmChannel
import com.nhom15.app_dat_xe.notification.channel.NotificationChannel
import com.nhom15.app_dat_xe.notification.channel.StompChannel
import com.nhom15.app_dat_xe.notification.dto.NotificationPayload
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationDispatcher(
    private val stomp: StompChannel,
    private val fcm: FcmChannel,
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) : NotificationPort {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(to: Recipient, payload: NotificationPayload): DeliveryResult {

        val envelope = WsEnvelope(payload.type, payload)
        persist(to, envelope)

        val result = when (payload.type.policy) {
            WS_ONLY ->
                if (trySend(stomp, to, envelope)) SENT_WS else UNREACHABLE

            WS_THEN_PUSH -> when {
                trySend(stomp, to, envelope) -> SENT_WS
                trySend(fcm, to, envelope) -> SENT_PUSH
                else -> UNREACHABLE
            }

            PUSH_ALSO -> {
                val ws = trySend(stomp, to, envelope)
                val push = trySend(fcm, to, envelope)
                when {
                    ws -> SENT_WS
                    push -> SENT_PUSH
                    else -> UNREACHABLE
                }
            }
        }
        log.info("notify type={} to={} policy={} result={}", payload.type, to, payload.type.policy, result)
        return result
    }

    private fun trySend(channel: NotificationChannel, to: Recipient, env: WsEnvelope<*>): Boolean =
        try {
            channel.isAvailable(to) && channel.send(to, env)
        } catch (e: Exception) {
            log.warn("channel {} failed for {}: {}", channel::class.simpleName, to, e.message)
            false
        }

    private fun persist(to: Recipient, envelope: WsEnvelope<*>) {
        try {
            jdbcTemplate.update(
                """INSERT INTO notification_history
                   (recipient_role, recipient_id, message_id, payload_type, payload_json)
                   VALUES (?, ?, ?, ?, ?) ON CONFLICT (message_id) DO NOTHING""".trimIndent(),
                to.role.name,
                to.id,
                envelope.messageId,
                envelope.type.name,
                objectMapper.writeValueAsString(envelope),
            )
        } catch (e: Exception) {
            log.warn("Không thể lưu lịch sử thông báo cho {}: {}", to, e.message)
        }
    }
}
