
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.ride.event.RideRequested
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BookingCreatedListener {

    @EventListener
    fun handleRideRequested(event: RideRequested) {
        println(
            "Nhận yêu cầu chuyến đi: rideId=${event.rideId}, " +
            "customerId=${event.customerId}"
        )
    }
}