package com.nhom15.app_dat_xe.realtime

import com.nhom15.app_dat_xe.realtime.dto.DriverLocationDTO
import org.springframework.stereotype.Service

@Service
class MockLocationServiceImpl : LocationService {
    override fun findNearby(lat: Double, log: Double, radius: Double): List<DriverLocationDTO> {
        return listOf(
            DriverLocationDTO(driverId = 1L, lat = lat + 0.001, log = log + 0.001, distance = 0.5),
            DriverLocationDTO(driverId = 2L, lat = lat + 0.005, log = log, distance = 1.2),
            DriverLocationDTO(driverId = 3L, lat = lat - 0.01, log = log + 0.02, distance = 2.5)
        )
    }
}