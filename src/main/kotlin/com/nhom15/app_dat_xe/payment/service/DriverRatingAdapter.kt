// Cập nhật điểm đánh giá trung bình của tài xế.

package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.port.DriverRatingPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class DriverRatingAdapter(
    private val jdbcTemplate: JdbcTemplate
) : DriverRatingPort {

    override fun updateDriverRating(
        driverId: Long,
        newRating: Int
    ) {

        if (newRating !in 1..5) {
            throw IllegalArgumentException(
                "Số sao phải nằm trong khoảng từ 1 đến 5"
            )
        }

        val sql = """
            UPDATE drivers
            SET average_rating = (
                SELECT AVG(stars)
                FROM ratings
                WHERE ratee_id = ?
                  AND direction = 'CUSTOMER_TO_DRIVER'
            )
            WHERE id = ?
        """.trimIndent()

        val updatedRows = jdbcTemplate.update(
            sql,
            driverId,
            driverId
        )

        if (updatedRows == 0) {
            throw IllegalArgumentException(
                "Không tìm thấy tài xế với id = $driverId"
            )
        }
    }
}