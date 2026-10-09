package com.nhom15.app_dat_xe.common.port

import com.nhom15.app_dat_xe.common.enums.Transmission

/**
 * Contract để booking-core kiểm tra xe thuộc khách hàng
 * và lấy snapshot hộp số phục vụ matching.
 */
interface CustomerVehicleQueryPort {
    fun findOwnedVehicle(vehicleId: Long, customerId: Long): CustomerVehicleSnapshot?
}

data class CustomerVehicleSnapshot(
    val id: Long,
    val ownerId: Long,
    val transmission: Transmission,
)
