
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.port.BookingRatingInfo
import com.nhom15.app_dat_xe.common.port.BookingRatingPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class BookingRatingAdapter(
    private val jdbcTemplate: JdbcTemplate
) : BookingRatingPort {

    override fun getRatingInfo(
        bookingId: Long
    ): BookingRatingInfo? {

        val sql = """
            SELECT
                customer_id,
                driver_id,
                status
            FROM bookings
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { resultSet, _ ->
                BookingRatingInfo(
                    customerId = resultSet.getLong("customer_id"),
                    driverId = resultSet.getObject("driver_id")
                        ?.let { (it as Number).toLong() },
                    status = resultSet.getString("status")
                )
            },
            bookingId
        ).firstOrNull()
    }

    override fun canRate(
        bookingId: Long,
        userId: Long
    ): Boolean {

        val booking = getRatingInfo(bookingId)
            ?: return false

        if (booking.status != "COMPLETED") {
            return false
        }

        return booking.customerId == userId ||
            booking.driverId == userId
    }
}