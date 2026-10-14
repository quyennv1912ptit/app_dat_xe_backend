package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.account.repository.DriverRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.payment.dto.WalletResponse
import com.nhom15.app_dat_xe.payment.dto.WalletTransactionResponse
import com.nhom15.app_dat_xe.ride.event.RideCompleted
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class PaymentService(
    private val jdbcTemplate: JdbcTemplate,
    private val driverRepository: DriverRepository,
    private val rideRepository: RideRepository,
) {
    @Transactional(readOnly = true)
    fun getDriverWallet(driverId: Long): WalletResponse {
        val driver = driverRepository.findById(driverId).orElseThrow {
            NotFoundException(ErrorCode.USER_NOT_FOUND)
        }
        val transactions = jdbcTemplate.query(
            """SELECT id, ride_id, type, amount, balance_after, description, created_at
               FROM wallet_transactions WHERE driver_id = ? ORDER BY created_at DESC, id DESC LIMIT 100""".trimIndent(),
            { row, _ ->
                WalletTransactionResponse(
                    id = row.getLong("id"), rideId = row.getObject("ride_id", java.lang.Long::class.java)?.toLong(),
                    type = row.getString("type"), amount = row.getLong("amount"),
                    balanceAfter = row.getLong("balance_after"), description = row.getString("description"),
                    createdAt = row.getTimestamp("created_at").toInstant(),
                )
            },
            driverId,
        )
        return WalletResponse(driver.incomeBalance, transactions)
    }

    /** Creates an idempotent pending record; gateway settlement is a separate future flow. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onRideCompleted(event: RideCompleted) {
        val ride = rideRepository.findById(event.rideId).orElse(null) ?: return
        jdbcTemplate.update(
            """INSERT INTO payments (ride_id, customer_id, driver_id, amount, method, status)
               VALUES (?, ?, ?, ?, ?, 'PENDING') ON CONFLICT (ride_id) DO NOTHING""".trimIndent(),
            event.rideId, event.customerId, event.driverId, event.finalFare.amount, ride.paymentMethod.name,
        )
    }
}
