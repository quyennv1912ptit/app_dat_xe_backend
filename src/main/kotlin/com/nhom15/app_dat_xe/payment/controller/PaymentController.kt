// Cung cấp các API để tạo, cập nhật và tra cứu Payment.

package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.payment.dto.CreatePaymentRequest
import com.nhom15.app_dat_xe.payment.dto.PaymentResponse
import com.nhom15.app_dat_xe.payment.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping
    fun createPayment(
        @Valid @RequestBody request: CreatePaymentRequest
    ): ResponseEntity<PaymentResponse> {

        val payment = paymentService.createPayment(request)

        return ResponseEntity.ok(payment)
    }

    @GetMapping("/booking/{bookingId}")
    fun getPaymentByBookingId(
        @PathVariable bookingId: Long
    ): ResponseEntity<PaymentResponse> {

        val payment = paymentService.getPaymentByBookingId(bookingId)

        return ResponseEntity.ok(payment)
    }

    // Đánh dấu Payment đã thanh toán thành công.
    @PostMapping("/{id}/pay")
    fun markAsPaid(
        @PathVariable id: Long
    ): ResponseEntity<PaymentResponse> {

        val payment = paymentService.markAsPaid(id)

        return ResponseEntity.ok(payment)
    }
}