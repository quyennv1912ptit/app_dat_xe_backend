package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.payment.dto.ValidateVoucherRequest
import com.nhom15.app_dat_xe.payment.dto.VoucherResponse
import com.nhom15.app_dat_xe.payment.entity.DiscountType
import com.nhom15.app_dat_xe.payment.entity.Voucher
import com.nhom15.app_dat_xe.payment.entity.VoucherUsage
import com.nhom15.app_dat_xe.payment.repository.VoucherRepository
import com.nhom15.app_dat_xe.payment.repository.VoucherUsageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class VoucherService(
    private val voucherRepository: VoucherRepository,
    private val voucherUsageRepository: VoucherUsageRepository
) {

    @Transactional
    fun validateVoucher(
        request: ValidateVoucherRequest
    ): VoucherResponse {

        val userId = request.userId
            ?: throw IllegalArgumentException("userId không được để trống")

        val orderAmount = request.orderAmount
            ?: throw IllegalArgumentException("orderAmount không được để trống")

        if (orderAmount <= 0) {
            throw IllegalArgumentException("Giá trị đơn hàng phải lớn hơn 0")
        }

        val code = request.code.trim().uppercase()

        val voucher = voucherRepository
            .findWithLockByCode(code)
            .orElseThrow {
                IllegalArgumentException("Voucher không tồn tại")
            }

        checkVoucherTime(voucher)

        checkMinimumOrderAmount(
            voucher = voucher,
            orderAmount = orderAmount
        )

        checkUsageLimit(voucher)

        checkUserAlreadyUsed(
            voucherId = voucher.id!!,
            userId = userId
        )

        val discountAmount = calculateDiscount(
            voucher = voucher,
            orderAmount = orderAmount
        )

        val finalAmount = orderAmount - discountAmount

        return VoucherResponse(
            code = voucher.code,
            discountType = voucher.discountType,
            value = voucher.value,
            orderAmount = orderAmount,
            discountAmount = discountAmount,
            finalAmount = finalAmount,
            minimumOrderAmount = voucher.minimumOrderAmount,
            maxDiscount = voucher.maxDiscount,
            usageLimit = voucher.usageLimit,
            usageCount = voucher.usageCount
        )
    }

    @Transactional
    fun useVoucher(
        code: String,
        userId: Long,
        bookingId: Long?
    ) {

        val voucher = voucherRepository
            .findWithLockByCode(code.trim().uppercase())
            .orElseThrow {
                IllegalArgumentException("Voucher không tồn tại")
            }

        checkVoucherTime(voucher)
        checkUsageLimit(voucher)

        checkUserAlreadyUsed(
            voucherId = voucher.id!!,
            userId = userId
        )

        val usage = VoucherUsage()

        usage.voucherId = voucher.id!!
        usage.userId = userId
        usage.bookingId = bookingId
        usage.usedAt = Instant.now()

        voucherUsageRepository.save(usage)

        voucher.usageCount++

        voucherRepository.save(voucher)
    }

    private fun checkVoucherTime(voucher: Voucher) {

        val now = Instant.now()

        if (now.isBefore(voucher.validFrom)) {
            throw IllegalArgumentException(
                "Voucher chưa bắt đầu có hiệu lực"
            )
        }

        if (now.isAfter(voucher.validTo)) {
            throw IllegalArgumentException(
                "Voucher đã hết hạn"
            )
        }
    }

    private fun checkMinimumOrderAmount(
        voucher: Voucher,
        orderAmount: Long
    ) {

        if (orderAmount < voucher.minimumOrderAmount) {
            throw IllegalArgumentException(
                "Đơn hàng phải có giá trị tối thiểu " +
                    "${voucher.minimumOrderAmount}đ"
            )
        }
    }

    private fun checkUsageLimit(voucher: Voucher) {

        val usageLimit = voucher.usageLimit

        if (usageLimit != null &&
            voucher.usageCount >= usageLimit
        ) {
            throw IllegalArgumentException(
                "Voucher đã hết lượt sử dụng"
            )
        }
    }

    private fun checkUserAlreadyUsed(
        voucherId: Long,
        userId: Long
    ) {

        val alreadyUsed =
            voucherUsageRepository
                .existsByVoucherIdAndUserId(
                    voucherId = voucherId,
                    userId = userId
                )

        if (alreadyUsed) {
            throw IllegalArgumentException(
                "Bạn đã sử dụng voucher này"
            )
        }
    }

    private fun calculateDiscount(
        voucher: Voucher,
        orderAmount: Long
    ): Long {

        val discount = when (voucher.discountType) {

            DiscountType.PERCENT -> {
                orderAmount * voucher.value / 100
            }

            DiscountType.FIXED -> {
                voucher.value
            }
        }

        val discountAfterMaxDiscount =
            if (voucher.maxDiscount != null) {
                minOf(
                    discount,
                    voucher.maxDiscount!!
                )
            } else {
                discount
            }

        return minOf(
            discountAfterMaxDiscount,
            orderAmount
        )
    }
}