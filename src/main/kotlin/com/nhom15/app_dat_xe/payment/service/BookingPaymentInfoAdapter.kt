// Lấy thông tin thanh toán của Booking để module Payment xử lý.

package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.domain.Money
import com.nhom15.app_dat_xe.common.port.BookingPaymentInfo
import com.nhom15.app_dat_xe.common.port.BookingPaymentInfoPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class BookingPaymentInfoAdapter(
    private val jdbcTemplate: JdbcTemplate
) : BookingPaymentInfoPort {

    override fun getPaymentInfo(
        bookingId: Long
    ): BookingPaymentInfo {

        val sql = """
            SELECT
                id,
                customer_id,
                driver_id,
                estimated_fare,
                distance_km
            FROM bookings
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            { resultSet, _ ->

                val estimatedFare =
                    resultSet.getLong("estimated_fare")

                val distanceKm =
                    resultSet.getDouble("distance_km")

                val driverId =
                    resultSet.getObject("driver_id")?.let {
                        (it as Number).toLong()
                    }

                BookingPaymentInfo(
                    bookingId = resultSet.getLong("id"),
                    customerId = resultSet.getLong("customer_id"),
                    driverId = driverId,
                    estimatedFare = Money(estimatedFare),
                    distanceKm = distanceKm,
                    voucherCode = null
                )
            },
            bookingId
        )
    }
}