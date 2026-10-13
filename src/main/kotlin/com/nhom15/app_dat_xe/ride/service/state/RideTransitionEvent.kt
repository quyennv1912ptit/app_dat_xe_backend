package com.nhom15.app_dat_xe.ride.service.state

/** Hành động nghiệp vụ yêu cầu thay đổi trạng thái ride. */
enum class RideTransitionEvent {
    START_SEARCH,
    ASSIGN_DRIVER,
    DRIVER_START_ARRIVING,
    DRIVER_ARRIVED,
    PICKUP_HANDOVER_CREATED,
    START_TRIP,
    ARRIVE_DESTINATION,
    DROPOFF_HANDOVER_CREATED,
    COMPLETE,
    CANCEL,
    NO_DRIVER_FOUND,
    EXPIRE,
}
