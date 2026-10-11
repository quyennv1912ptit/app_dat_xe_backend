// Xử lý nghiệp vụ tạo, cập nhật và truy vấn thanh toán.

package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.enums.PaymentStatus
import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.event.PaymentCompleted
import com.nhom15.app_dat_xe.common.port.BookingPaymentInfoPort
import com.nhom15.app_dat_xe.payment.dto.CreatePaymentRequest
import com.nhom15.app_dat_xe.payment.dto.PaymentResponse
import com.nhom15.app_dat_xe.payment.entity.Payment
import com.nhom15.app_dat_xe.payment.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val bookingPaymentInfoPort: BookingPaymentInfoPort,
    private val paymentCalculationService: PaymentCalculationService,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun createPayment(request: CreatePaymentRequest): PaymentResponse {

        val bookingId = request.bookingId
            ?: throw IllegalArgumentException("bookingId không được để trống")

        val method = request.method
            ?: throw IllegalArgumentException("payment method không được để trống")

        val existingPayment = paymentRepository
            .findByBookingId(bookingId)
            .orElse(null)

        if (existingPayment != null) {
            return toResponse(existingPayment)
        }

        val bookingInfo = bookingPaymentInfoPort.getPaymentInfo(bookingId)

        val payment = Payment()

        payment.bookingId = bookingInfo.bookingId
        payment.customerId = bookingInfo.customerId
        payment.driverId = bookingInfo.driverId
        payment.amount =
            paymentCalculationService.calculateAmount(bookingInfo)
        payment.method = method
        payment.status = PaymentStatus.PENDING
        payment.createdAt = Instant.now()

        val savedPayment = paymentRepository.save(payment)

        return toResponse(savedPayment)
    }

    @Transactional
    fun markAsPaid(paymentId: Long): PaymentResponse {

        val payment = paymentRepository.findById(paymentId)
            .orElseThrow {
                IllegalArgumentException(
                    "Không tìm thấy payment với id = $paymentId"
                )
            }

        if (payment.status == PaymentStatus.PAID) {
            return toResponse(payment)
        }

        payment.status = PaymentStatus.PAID
        payment.paidAt = Instant.now()
        payment.updatedAt = Instant.now()

        val savedPayment = paymentRepository.save(payment)

        val driverId = savedPayment.driverId

        if (driverId != null) {

            val event = PaymentCompleted(
                bookingId = savedPayment.bookingId,
                paymentId = savedPayment.id!!,
                customerId = savedPayment.customerId,
                driverId = driverId,
                amount = com.nhom15.app_dat_xe.common.domain.Money(
                    savedPayment.amount
                ),
                method = savedPayment.method
            )

            eventPublisher.publish(event)
        }

        return toResponse(savedPayment)
    }

    fun getPaymentByBookingId(bookingId: Long): PaymentResponse {

        val payment = paymentRepository.findByBookingId(bookingId)
            .orElseThrow {
                IllegalArgumentException(
                    "Không tìm thấy payment của booking = $bookingId"
                )
            }

        return toResponse(payment)
    }

    private fun toResponse(payment: Payment): PaymentResponse {

        return PaymentResponse(
            id = payment.id!!,
            bookingId = payment.bookingId,
            customerId = payment.customerId,
            driverId = payment.driverId,
            amount = payment.amount,
            method = payment.method,
            status = payment.status,
            paidAt = payment.paidAt,
            createdAt = payment.createdAt
        )
    }
}