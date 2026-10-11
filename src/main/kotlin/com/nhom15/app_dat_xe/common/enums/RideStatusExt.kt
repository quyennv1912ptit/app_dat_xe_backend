package com.nhom15.app_dat_xe.common.enums

private val TRANSITIONS: Map<RideStatus, Set<RideStatus>> = mapOf(
    RideStatus.PENDING to setOf(
        RideStatus.SEARCHING, RideStatus.CANCELLED, RideStatus.EXPIRED
    ),
    RideStatus.SEARCHING to setOf(
        RideStatus.DRIVER_ASSIGNED, RideStatus.NO_DRIVER_FOUND, RideStatus.CANCELLED
    ),
    RideStatus.DRIVER_ASSIGNED to setOf(
        RideStatus.DRIVER_ARRIVING, RideStatus.CANCELLED
    ),
    RideStatus.DRIVER_ARRIVING to setOf(
        RideStatus.DRIVER_ARRIVED, RideStatus.CANCELLED
    ),
    RideStatus.DRIVER_ARRIVED to setOf(
        RideStatus.VEHICLE_HANDOVER, RideStatus.CANCELLED
    ),
    RideStatus.VEHICLE_HANDOVER to setOf(
        RideStatus.IN_PROGRESS, RideStatus.CANCELLED
    ),
    RideStatus.IN_PROGRESS to setOf(
        RideStatus.ARRIVED_DESTINATION
    ),
    RideStatus.ARRIVED_DESTINATION to setOf(
        RideStatus.VEHICLE_RETURNED
    ),
    RideStatus.VEHICLE_RETURNED to setOf(
        RideStatus.COMPLETED
    )
)

/** Trạng thái kết thúc: không chuyển đi đâu nữa. */
val RideStatus.isTerminal: Boolean
    get() = this == RideStatus.COMPLETED ||
        this == RideStatus.CANCELLED ||
        this == RideStatus.NO_DRIVER_FOUND ||
        this == RideStatus.EXPIRED

/** Có được chuyển từ trạng thái hiện tại sang [next] không. */
fun RideStatus.canTransitionTo(next: RideStatus): Boolean =
    TRANSITIONS[this]?.contains(next) == true
