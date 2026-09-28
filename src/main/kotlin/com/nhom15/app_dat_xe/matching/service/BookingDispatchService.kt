package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.event.BookingCreated
import com.nhom15.app_dat_xe.realtime.LocationService
import com.nhom15.app_dat_xe.realtime.dto.DriverLocationDTO
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class BookingDispatchService (
    val locationService: LocationService,
    val selectionService: DriverSelectionService
) {
    @EventListener
    fun handleBookingCreated(event: BookingCreated) {
        val radius = 2.0
        val nearby: List<DriverLocationDTO> = locationService.findNearby(event.pickup.lat, event.pickup.lng, radius)
    }
}