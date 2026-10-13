package com.nhom15.app_dat_xe.config

import com.nhom15.app_dat_xe.notification.ws.StompAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val authInterceptor: StompAuthInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.setApplicationDestinationPrefixes("/app")
        config.setUserDestinationPrefix("/user")

        // Scheduler riêng cho heartbeat, không khai báo @Bean để khỏi trùng TaskScheduler của Spring Boot.
        val heartbeatScheduler = ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("stomp-heartbeat-")
            setDaemon(true)
            initialize()
        }

        // /topic: giữ nguyên cho tracking vị trí.  /queue: thông báo riêng từng người (/user/queue/...).
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(longArrayOf(10_000, 10_000))
            .setTaskScheduler(heartbeatScheduler)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Giữ nguyên endpoint tracking cũ
        registry.addEndpoint("/ws-location").setAllowedOriginPatterns("*").withSockJS()
        // Endpoint cho thông báo (WebSocket thuần, app mobile)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
    }

    /** Mọi frame từ client (cả /ws và /ws-location) đều đi qua interceptor đăng nhập. */
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(authInterceptor)
    }
}