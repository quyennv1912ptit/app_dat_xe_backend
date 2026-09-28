package com.nhom15.app_dat_xe.matching.entity

data class DriverMatchStats(
    val driverId: Long,
    var offersReceived: Int = 0,
    var offersAccepted: Int = 0,
    var acceptanceRate: Double = 0.0,
    var cancelCount: Int = 0
)