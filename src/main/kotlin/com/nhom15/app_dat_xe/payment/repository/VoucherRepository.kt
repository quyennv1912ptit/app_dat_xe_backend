package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.entity.Voucher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import jakarta.persistence.LockModeType
import java.util.Optional

interface VoucherRepository : JpaRepository<Voucher, Long> {

    fun findByCode(code: String): Optional<Voucher>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockByCode(code: String): Optional<Voucher>
}