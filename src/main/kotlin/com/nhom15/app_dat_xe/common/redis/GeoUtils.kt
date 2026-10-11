package com.nhom15.app_dat_xe.common.redis

import java.time.Duration

object RedisTtl {
    val DRIVER_ALIVE: Duration = Duration.ofSeconds(30)
    val DRIVER_LOCATION: Duration = Duration.ofSeconds(60)
    val DRIVER_LOCK: Duration = Duration.ofSeconds(20)
    val DRIVER_STATS: Duration = Duration.ofDays(1)
    val OFFER_TIMEOUT: Duration = Duration.ofSeconds(15)
    val RIDE_AFTER_END: Duration = Duration.ofHours(1)
    val IDEMPOTENCY: Duration = Duration.ofHours(24)
}