package com.nhom15.app_dat_xe.common.event

/**
 * Các module chỉ phụ thuộc interface này để phát event.
 * Sau này đổi sang Redis/Kafka chỉ cần viết một cài đặt khác.
 */
interface EventPublisher {
    fun publish(event: DomainEvent)
}
