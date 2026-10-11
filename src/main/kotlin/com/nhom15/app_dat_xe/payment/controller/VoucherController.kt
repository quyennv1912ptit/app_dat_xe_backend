package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.payment.dto.ValidateVoucherRequest
import com.nhom15.app_dat_xe.payment.dto.VoucherResponse
import com.nhom15.app_dat_xe.payment.service.VoucherService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/vouchers")
class VoucherController(
    private val voucherService: VoucherService
) {

    @PostMapping("/validate")
    fun validateVoucher(
        @Valid @RequestBody request: ValidateVoucherRequest
    ): ResponseEntity<VoucherResponse> {

        val response =
            voucherService.validateVoucher(request)

        return ResponseEntity.ok(response)
    }
}