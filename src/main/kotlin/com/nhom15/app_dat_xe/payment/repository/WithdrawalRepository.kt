
package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.dto.WithdrawalResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Repository
class WithdrawalRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun createWithdrawal(
        driverId: Long,
        amount: BigDecimal
    ): WithdrawalResponse? {

        // Chỉ trừ tiền nếu tài xế có đủ số dư.
        val updatedRows = jdbcTemplate.update(
            """
            UPDATE drivers
            SET wallet_balance = wallet_balance - ?,
                pending_payout = COALESCE(pending_payout, 0) + ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND wallet_balance >= ?
            """.trimIndent(),
            amount,
            amount,
            driverId,
            amount
        )

        if (updatedRows == 0) {
            return null
        }

        // Ghi nhận yêu cầu rút tiền đang chờ xử lý.
        val transactionId = jdbcTemplate.queryForObject(
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
            VALUES (?, NULL, ?, 'WITHDRAW', 'PENDING',
                    'Yeu cau rut tien', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            RETURNING id
            """.trimIndent(),
            Long::class.java,
            driverId,
            amount
        ) ?: throw IllegalStateException(
            "Không tạo được giao dịch rút tiền"
        )

        return jdbcTemplate.queryForObject(
            """
            SELECT id, driver_id, amount, status, description, created_at
            FROM wallet_transactions
            WHERE id = ?
            """.trimIndent(),
            { rs, _ ->
                WithdrawalResponse(
                    transactionId = rs.getLong("id"),
                    driverId = rs.getLong("driver_id"),
                    amount = rs.getBigDecimal("amount"),
                    status = rs.getString("status"),
                    description = rs.getString("description"),
                    createdAt = rs.getTimestamp("created_at")
                        ?.toLocalDateTime()
                )
            },
            transactionId
        )
    }

    fun driverExists(driverId: Long): Boolean {
        return jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM drivers WHERE id = ?)",
            Boolean::class.java,
            driverId
        ) ?: false
    }


@Transactional
fun processWithdrawal(
    transactionId: Long,
    newStatus: String
): Boolean {
    // Khóa giao dịch để ngăn hai yêu cầu xử lý đồng thời.
    val rows = jdbcTemplate.query(
        """
        SELECT id, driver_id, amount, status
        FROM wallet_transactions
        WHERE id = ?
          AND type = 'WITHDRAW'
        FOR UPDATE
        """.trimIndent(),
        { rs, _ ->
            arrayOf(
                rs.getLong("driver_id"),
                rs.getBigDecimal("amount"),
                rs.getString("status")
            )
        },
        transactionId
    )

    if (rows.isEmpty()) {
        return false
    }

    val transaction = rows.first()
    val driverId = transaction[0] as Long
    val amount = transaction[1] as BigDecimal
    val currentStatus = transaction[2] as String

    // Chỉ xử lý giao dịch còn đang chờ.
    if (currentStatus != "PENDING") {
        return false
    }

    // Khi rút tiền thất bại, hoàn lại số dư.
    if (newStatus == "FAILED") {
        val updatedDrivers = jdbcTemplate.update(
            """
            UPDATE drivers
            SET wallet_balance = COALESCE(wallet_balance, 0) + ?,
                pending_payout = COALESCE(pending_payout, 0) - ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND COALESCE(pending_payout, 0) >= ?
            """.trimIndent(),
            amount,
            amount,
            driverId,
            amount
        )

        if (updatedDrivers == 0) {
            throw IllegalStateException(
                "Không thể hoàn tiền: số tiền chờ xử lý không hợp lệ"
            )
        }
    } else {
        // Khi thành công, chỉ giảm khoản tiền đang chờ.
        val updatedDrivers = jdbcTemplate.update(
            """
            UPDATE drivers
            SET pending_payout = COALESCE(pending_payout, 0) - ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND COALESCE(pending_payout, 0) >= ?
            """.trimIndent(),
            amount,
            driverId,
            amount
        )

        if (updatedDrivers == 0) {
            throw IllegalStateException(
                "Không thể hoàn tất: số tiền chờ xử lý không hợp lệ"
            )
        }
    }

    val updatedTransactions = jdbcTemplate.update(
        """
        UPDATE wallet_transactions
        SET status = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
          AND status = 'PENDING'
        """.trimIndent(),
        newStatus,
        transactionId
    )

    if (updatedTransactions != 1) {
        throw IllegalStateException(
            "Không thể cập nhật trạng thái giao dịch"
        )
    }

    return true
}
}