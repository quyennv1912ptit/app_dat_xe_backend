package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.common.enums.Transmission
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
        val transmission = Transmission.AUTO
        val nearby: List<DriverLocationDTO> = locationService.findNearby(event.pickup.lat, event.pickup.lng, radius)

        val rankedDrivers = selectionService.filterAndRankDrivers(nearby, transmission, radius)

        if (rankedDrivers.isEmpty()) {
            println("Không tìm thấy tài xế phù hợp, chuẩn bị mở rộng bán kính...")
            // Logic tăng bán kính (radius) sẽ nằm ở đây
        } else {
            println("Sẵn sàng phát offer cho tài xế tốt nhất: ${rankedDrivers.first().driverId}")
            // Bắt đầu vòng lặp bắn offer cho RankedDrivers
        }
    }
}