
package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.payment.dto.CreateWithdrawalRequest
import com.nhom15.app_dat_xe.payment.dto.WithdrawalResponse
import com.nhom15.app_dat_xe.payment.service.WithdrawalService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/drivers/{driverId}/withdrawals")
class WithdrawalController(
    private val withdrawalService: WithdrawalService
) {
    @PostMapping
    fun createWithdrawal(
        @PathVariable driverId: Long,
        @RequestBody request: CreateWithdrawalRequest
    ): WithdrawalResponse {
        return withdrawalService.createWithdrawal(
            driverId,
            request.amount
        )
    }
}