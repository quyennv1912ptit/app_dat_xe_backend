package com.nhom15.app_dat_xe.common.port

import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.enums.VehicleType

/**
 * Contract để ride-core kiểm tra xe thuộc khách hàng
 * và lấy snapshot hộp số phục vụ matching.
 */
interface CustomerVehicleQueryPort {
    fun findOwnedVehicle(vehicleId: Long, customerId: Long): CustomerVehicleSnapshot?
}

data class CustomerVehicleSnapshot(
    val id: Long,
    val ownerId: Long,
    val transmission: Transmission,
    // Legacy vehicles without vehicle_type describe cars; newer records supply it explicitly.
    val vehicleType: VehicleType = VehicleType.CAR,
    val licensePlate: String? = null,
)
