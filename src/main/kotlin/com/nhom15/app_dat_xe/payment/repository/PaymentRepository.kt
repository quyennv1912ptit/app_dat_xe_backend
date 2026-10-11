package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<Payment, Long> {

    fun findByBookingId(bookingId: Long): Optional<Payment>

    fun existsByBookingId(bookingId: Long): Boolean
}