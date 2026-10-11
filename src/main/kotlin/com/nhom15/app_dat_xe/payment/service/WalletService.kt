package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.payment.dto.WalletResponse
import com.nhom15.app_dat_xe.payment.dto.WalletTransactionResponse
import com.nhom15.app_dat_xe.payment.repository.WalletRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class WalletService(
    private val walletRepository: WalletRepository
) {
    fun getWallet(driverId: Long): WalletResponse {
        validateDriverId(driverId)

        return walletRepository.getWallet(driverId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tài xế"
            )
    }

    fun getTransactions(
        driverId: Long,
        page: Int,
        size: Int
    ): List<WalletTransactionResponse> {
        validateDriverId(driverId)

        if (page < 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Số trang phải lớn hơn hoặc bằng 0"
            )
        }

        if (size !in 1..100) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Số giao dịch mỗi trang phải từ 1 đến 100"
            )
        }

        if (!walletRepository.driverExists(driverId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tài xế"
            )
        }

        val offset = page.toLong() * size

        if (offset > Int.MAX_VALUE) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Số trang quá lớn"
            )
        }

        return walletRepository.getTransactions(
            driverId = driverId,
            limit = size,
            offset = offset.toInt()
        )
    }

    private fun validateDriverId(driverId: Long) {
        if (driverId <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID tài xế không hợp lệ"
            )
        }
    }
}