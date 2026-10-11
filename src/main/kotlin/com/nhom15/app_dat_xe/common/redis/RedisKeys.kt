package com.nhom15.app_dat_xe.common.redis

object RedisKeys {

    // ===== Driver =====
    const val GEO_KEY = "available_drivers"
    private const val ALIVE_PREFIX = "driver:alive:"

    fun driverAlive(id: String) = "$ALIVE_PREFIX$id"
    fun driverStatus(id: String) = "driver:$id:status"
    fun driverLocation(id: String) = "driver:$id:location"
    fun driverLock(id: String) = "driver:$id:lock"
    fun driverCurrentRide(id: String) = "driver:$id:current_ride"
    fun driverStats(id: String) = "driver:$id:stats"

    // ===== Ride =====
    fun rideState(id: String) = "ride:$id:state"
    fun rideCandidates(id: String) = "ride:$id:candidates"
    fun rideRejected(id: String) = "ride:$id:rejected"
    fun rideTrack(id: String) = "ride:$id:track"
    fun rideEvents(id: String) = "ride:$id:events"
    const val DISPATCH_TIMEOUTS = "dispatch:timeouts"
    const val SCHEDULED_RIDES = "scheduled:rides"

    // ===== Customer (chỉ phần liên quan chuyến) =====
    fun customerActiveRide(id: String) = "customer:$id:active_ride"

    // ===== Dùng chung cho luồng chuyến =====
    fun idempotency(key: String) = "idem:$key"
    fun rideLock(id: String) = "lock:ride:$id"
}

