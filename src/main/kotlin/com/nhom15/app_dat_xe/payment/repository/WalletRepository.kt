package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.dto.WalletResponse
import com.nhom15.app_dat_xe.payment.dto.WalletTransactionResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class WalletRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun getWallet(driverId: Long): WalletResponse? {
        val sql = """
            SELECT
                d.id AS driver_id,
                COALESCE(d.wallet_balance, 0) AS balance,
                COALESCE(d.pending_payout, 0) AS pending_payout,

                COALESCE((
                    SELECT SUM(b.final_fare)
                    FROM bookings b
                    WHERE b.driver_id = d.id
                      AND b.status = 'COMPLETED'
                ), 0) AS gross_income,

                COALESCE((
                    SELECT SUM(w.amount)
                    FROM wallet_transactions w
                    WHERE w.driver_id = d.id
                      AND w.type = 'TRIP_EARNING'
                      AND w.status = 'SUCCESS'
                ), 0) AS net_income,

                (
                    SELECT COUNT(*)
                    FROM bookings b
                    WHERE b.driver_id = d.id
                      AND b.status = 'COMPLETED'
                      AND b.final_fare IS NOT NULL
                ) AS total_completed_trips

            FROM drivers d
            WHERE d.id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                val grossIncome = rs.getBigDecimal("gross_income")
                val netIncome = rs.getBigDecimal("net_income")

                WalletResponse(
                    driverId = rs.getLong("driver_id"),
                    balance = rs.getBigDecimal("balance"),
                    pendingPayout = rs.getBigDecimal("pending_payout"),
                    grossIncome = grossIncome,
                    netIncome = netIncome,
                    deductedFees = grossIncome.subtract(netIncome),
                    totalCompletedTrips =
                        rs.getLong("total_completed_trips")
                )
            },
            driverId
        ).firstOrNull()
    }

    fun getTransactions(
        driverId: Long,
        limit: Int,
        offset: Int
    ): List<WalletTransactionResponse> {
        val sql = """
            SELECT
                w.id,
                w.booking_id,
                w.amount,
                w.type,
                w.status,
                w.description,
                w.created_at
            FROM wallet_transactions w
            WHERE w.driver_id = ?
            ORDER BY w.created_at DESC, w.id DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                val bookingIdValue = rs.getLong("booking_id")
                val bookingId =
                    if (rs.wasNull()) null else bookingIdValue

                WalletTransactionResponse(
                    id = rs.getLong("id"),
                    bookingId = bookingId,
                    amount = rs.getBigDecimal("amount"),
                    type = rs.getString("type"),
                    status = rs.getString("status"),
                    description = rs.getString("description"),
                    createdAt = rs.getTimestamp("created_at")
                        ?.toLocalDateTime()
                )
            },
            driverId,
            limit,
            offset
        )
    }

    fun driverExists(driverId: Long): Boolean {
        val sql = """
            SELECT EXISTS (
                SELECT 1
                FROM drivers
                WHERE id = ?
            )
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Boolean::class.java,
            driverId
        ) ?: false
    }
}