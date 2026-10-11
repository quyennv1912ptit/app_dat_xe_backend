
package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.payment.dto.ProcessWithdrawalRequest
import com.nhom15.app_dat_xe.payment.service.WithdrawalService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/withdrawals")
class WithdrawalAdminController(
    private val withdrawalService: WithdrawalService
) {

    @PostMapping("/{transactionId}/status")
    fun processWithdrawal(
        @PathVariable transactionId: Long,
        @RequestBody request: ProcessWithdrawalRequest
    ): Map<String, String> {
        withdrawalService.processWithdrawal(
            transactionId,
            request
        )

        return mapOf(
            "message" to "Đã cập nhật yêu cầu rút tiền",
            "status" to request.status.trim().uppercase()
        )
    }
}