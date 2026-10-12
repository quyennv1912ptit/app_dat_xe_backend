package com.nhom15.app_dat_xe.ride.service.state

data class RideTransitionCommand(
    val rideId: Long,
    val event: RideTransitionEvent,
    val actor: RideActor,
    val note: String? = null,
    val cancelReason: String? = null,
)
