
package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.event.RidePaymentCompleted
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Repository
class DriverEarningRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun recordEarning(event: RidePaymentCompleted): Boolean {
        val amount = BigDecimal.valueOf(event.amount.amount)

        require(amount > BigDecimal.ZERO) {
            "Số tiền thu nhập phải lớn hơn 0"
        }

        // Chỉ tạo một giao dịch thu nhập cho mỗi chuyến đi của tài xế.
        val inserted = jdbcTemplate.update(
            """
            INSERT INTO wallet_transactions (
                driver_id,
                booking_id,
                amount,
                type,
                status,
                description,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, 'TRIP_EARNING', 'SUCCESS', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (driver_id, booking_id, type)
            WHERE type = 'TRIP_EARNING' AND booking_id IS NOT NULL
            DO NOTHING
            """.trimIndent(),
            event.driverId,
            event.rideId,
            amount,
            "Thu nhập chuyến đi #${event.rideId}"
        )

        // Giao dịch đã tồn tại thì không cộng số dư thêm lần nữa.
        if (inserted == 0) {
            return false
        }

        val updated = jdbcTemplate.update(
            """
            UPDATE drivers
            SET wallet_balance = COALESCE(wallet_balance, 0) + ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """.trimIndent(),
            amount,
            event.driverId
        )

        if (updated != 1) {
            throw IllegalStateException(
                "Không tìm thấy tài xế id=${event.driverId}"
            )
        }

        return true
    }
}