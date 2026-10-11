// Cung cấp các thao tác truy vấn và lưu Rating vào cơ sở dữ liệu.

package com.nhom15.app_dat_xe.payment.repository

import com.nhom15.app_dat_xe.payment.entity.Rating
import org.springframework.data.jpa.repository.JpaRepository

interface RatingRepository : JpaRepository<Rating, Long> {

    fun existsByBookingIdAndRaterId(
        bookingId: Long,
        raterId: Long
    ): Boolean

    fun findByRateeIdOrderByCreatedAtDesc(
        rateeId: Long
    ): List<Rating>
}