package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.payment.dto.WalletResponse
import com.nhom15.app_dat_xe.payment.dto.WalletTransactionResponse
import com.nhom15.app_dat_xe.payment.service.WalletService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/drivers/{driverId}/wallet")
class WalletController(
    private val walletService: WalletService
) {
    @GetMapping
    fun getWallet(
        @PathVariable driverId: Long
    ): WalletResponse {
        return walletService.getWallet(driverId)
    }

    @GetMapping("/transactions")
    fun getTransactions(
        @PathVariable driverId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): List<WalletTransactionResponse> {
        return walletService.getTransactions(
            driverId = driverId,
            page = page,
            size = size
        )
    }
}