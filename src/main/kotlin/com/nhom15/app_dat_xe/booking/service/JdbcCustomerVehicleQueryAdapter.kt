package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.port.CustomerVehicleQueryPort
import com.nhom15.app_dat_xe.common.port.CustomerVehicleSnapshot
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

/** Reads the customer's vehicle without exposing another customer's vehicle to Booking. */
@Service
class JdbcCustomerVehicleQueryAdapter(
    private val jdbcTemplate: JdbcTemplate,
) : CustomerVehicleQueryPort {
    override fun findOwnedVehicle(vehicleId: Long, customerId: Long): CustomerVehicleSnapshot? {
        if (vehicleId <= 0 || customerId <= 0) return null

        return jdbcTemplate.query(
            """
            SELECT vehicle.id, vehicle.user_id, vehicle.transmission_type
            FROM vehicles vehicle
            JOIN users customer ON customer.id = vehicle.user_id
            WHERE vehicle.id = ? AND vehicle.user_id = ?
            """.trimIndent(),
            { row, _ ->
                val transmissionType = row.getString("transmission_type")
                val transmission = Transmission.entries.find { it.name == transmissionType }
                    ?: throw BadRequestException(message = "Xe phải có transmission_type là AUTO hoặc MANUAL")
                CustomerVehicleSnapshot(
                    id = row.getLong("id"),
                    ownerId = row.getLong("user_id"),
                    transmission = transmission,
                )
            },
            vehicleId,
            customerId,
        ).firstOrNull()
    }
}
