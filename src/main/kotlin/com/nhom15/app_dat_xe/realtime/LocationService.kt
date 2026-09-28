package com.nhom15.app_dat_xe.realtime

import com.nhom15.app_dat_xe.realtime.dto.DriverLocationDTO

interface LocationService {
    fun findNearby(lat: Double, log: Double, radius: Double) : List<DriverLocationDTO>
}