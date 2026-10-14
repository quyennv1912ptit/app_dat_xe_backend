package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.payment.dto.WalletResponse
import com.nhom15.app_dat_xe.payment.service.PaymentService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/driver/me/wallet")
@PreAuthorize("hasRole('DRIVER')")
class DriverWalletController(private val paymentService: PaymentService) {
    @GetMapping
    fun getWallet(@CurrentUser actor: AuthUser): ApiResponse<WalletResponse> =
        ApiResponse.ok(paymentService.getDriverWallet(actor.id))
}
