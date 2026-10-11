
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.payment.dto.WithdrawalResponse
import com.nhom15.app_dat_xe.payment.repository.WithdrawalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import com.nhom15.app_dat_xe.payment.dto.ProcessWithdrawalRequest

@Service
class WithdrawalService(
    private val withdrawalRepository: WithdrawalRepository
) {
    fun createWithdrawal(
        driverId: Long,
        amount: BigDecimal
    ): WithdrawalResponse {
        if (driverId <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID tài xế không hợp lệ"
            )
        }

        if (amount <= BigDecimal.ZERO) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Số tiền rút phải lớn hơn 0"
            )
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Số tiền chỉ được có tối đa 2 chữ số thập phân"
            )
        }

        if (!withdrawalRepository.driverExists(driverId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tài xế"
            )
        }

        return withdrawalRepository.createWithdrawal(driverId, amount)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Số dư ví không đủ để rút tiền"
            )
    }

fun processWithdrawal(
    transactionId: Long,
    request: ProcessWithdrawalRequest
) {
    if (transactionId <= 0) {
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "ID giao dịch không hợp lệ"
        )
    }

    val newStatus = request.status.trim().uppercase()

    if (newStatus != "SUCCESS" && newStatus != "FAILED") {
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Trạng thái chỉ được là SUCCESS hoặc FAILED"
        )
    }

    val processed = withdrawalRepository.processWithdrawal(
        transactionId,
        newStatus
    )

    if (!processed) {
        throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Không tìm thấy giao dịch rút tiền hoặc giao dịch đã được xử lý"
        )
    }
}

}