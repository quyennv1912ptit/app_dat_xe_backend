package com.nhom15.app_dat_xe.common.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Cài đặt mặc định bằng ApplicationEvent của Spring (trong cùng một ứng dụng).
 *
 * Bên nghe nên dùng:
 *   @Async
 *   @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
 *   fun on(e: BookingCreated) { ... }
 * để đảm bảo dữ liệu đã commit rồi mới xử lý và không chặn luồng của bên phát.
 */
@Component
class SpringEventPublisher(
    private val delegate: ApplicationEventPublisher
) : EventPublisher {

    override fun publish(event: DomainEvent) {
        delegate.publishEvent(event)
    }
}
