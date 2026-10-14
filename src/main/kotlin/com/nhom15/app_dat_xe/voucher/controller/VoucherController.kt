package com.nhom15.app_dat_xe.voucher.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.voucher.dto.VoucherResponse
import com.nhom15.app_dat_xe.voucher.service.VoucherService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/vouchers")
class VoucherController(private val voucherService: VoucherService) {
    @GetMapping
    fun list(@CurrentUser actor: AuthUser): ApiResponse<List<VoucherResponse>> =
        ApiResponse.ok(voucherService.listAvailable(actor))
}
