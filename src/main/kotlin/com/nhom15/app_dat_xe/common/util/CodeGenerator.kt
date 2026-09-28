package com.nhom15.app_dat_xe.common.util

import java.security.SecureRandom
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Sinh mã booking dễ đọc, ví dụ BK260923-0001. Ngày tính theo giờ Việt Nam. */
object CodeGenerator {

    private val VN_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd")

    // Bỏ các ký tự dễ nhầm: 0/O, 1/I
    private const val ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val random = SecureRandom()

    /**
     * Mã theo số thứ tự trong ngày: BK260923-0001, BK260923-0002...
     * [sequence] do booking-core cấp (ví dụ Redis INCR theo key ngày, hoặc sequence trong DB).
     */
    fun bookingCode(sequence: Long, date: LocalDate = LocalDate.now(VN_ZONE)): String {
        require(sequence >= 0) { "sequence phải >= 0" }
        return "BK${date.format(DATE_FORMAT)}-${sequence.toString().padStart(4, '0')}"
    }

    /**
     * Mã có hậu tố ngẫu nhiên: BK260923-7K3Q. Dùng khi chưa có bộ đếm.
     * Cột code trong DB nên để unique và thử lại nếu trùng.
     */
    fun randomBookingCode(date: LocalDate = LocalDate.now(VN_ZONE)): String {
        val suffix = (1..4).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")
        return "BK${date.format(DATE_FORMAT)}-$suffix"
    }
}
