
package com.nhom15.app_dat_xe.payment.service

import com.nhom15.app_dat_xe.common.port.BookingRatingPort
import com.nhom15.app_dat_xe.common.port.DriverRatingPort
import com.nhom15.app_dat_xe.payment.dto.CreateRatingRequest
import com.nhom15.app_dat_xe.payment.dto.RatingResponse
import com.nhom15.app_dat_xe.payment.entity.Rating
import com.nhom15.app_dat_xe.payment.repository.RatingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RatingService(
    private val ratingRepository: RatingRepository,
    private val bookingRatingPort: BookingRatingPort,
    private val driverRatingPort: DriverRatingPort
) {

    @Transactional
    fun createRating(
        request: CreateRatingRequest,
        raterId: Long
    ): RatingResponse {

        val bookingId = request.bookingId
            ?: throw IllegalArgumentException(
                "bookingId không được để trống"
            )

        val rateeId = request.rateeId
            ?: throw IllegalArgumentException(
                "rateeId không được để trống"
            )

        val direction = request.direction
            ?: throw IllegalArgumentException(
                "direction không được để trống"
            )

        if (request.stars !in 1..5) {
            throw IllegalArgumentException(
                "Số sao phải nằm trong khoảng từ 1 đến 5"
            )
        }

        val booking = bookingRatingPort.getRatingInfo(bookingId)
            ?: throw IllegalArgumentException(
                "Booking không tồn tại"
            )

        if (booking.status != "COMPLETED") {
            throw IllegalArgumentException(
                "Chỉ có thể đánh giá sau khi chuyến đi hoàn thành"
            )
        }

        val canRate = bookingRatingPort.canRate(
            bookingId = bookingId,
            userId = raterId
        )

        if (!canRate) {
            throw IllegalArgumentException(
                "Người dùng không có quyền đánh giá Booking này"
            )
        }

        when (direction) {
            "CUSTOMER_TO_DRIVER" -> {
                if (raterId != booking.customerId) {
                    throw IllegalArgumentException(
                        "Chỉ khách hàng của chuyến đi mới được đánh giá tài xế"
                    )
                }

                val driverId = booking.driverId
                    ?: throw IllegalArgumentException(
                        "Booking chưa có tài xế"
                    )

                if (rateeId != driverId) {
                    throw IllegalArgumentException(
                        "Chỉ được đánh giá tài xế của chuyến đi này"
                    )
                }
            }

            "DRIVER_TO_CUSTOMER" -> {
                val driverId = booking.driverId
                    ?: throw IllegalArgumentException(
                        "Booking chưa có tài xế"
                    )

                if (raterId != driverId) {
                    throw IllegalArgumentException(
                        "Chỉ tài xế của chuyến đi mới được đánh giá khách hàng"
                    )
                }

                if (rateeId != booking.customerId) {
                    throw IllegalArgumentException(
                        "Chỉ được đánh giá khách hàng của chuyến đi này"
                    )
                }
            }

            else -> throw IllegalArgumentException(
                "Direction đánh giá không hợp lệ"
            )
        }

        val alreadyRated =
            ratingRepository.existsByBookingIdAndRaterId(
                bookingId = bookingId,
                raterId = raterId
            )

        if (alreadyRated) {
            throw IllegalArgumentException(
                "Người dùng đã đánh giá Booking này"
            )
        }

        val rating = Rating()

        rating.bookingId = bookingId
        rating.raterId = raterId
        rating.rateeId = rateeId
        rating.stars = request.stars
        rating.tags = request.tags
        rating.comment = request.comment
        rating.direction = direction
        rating.createdAt = Instant.now()

        val savedRating = ratingRepository.save(rating)

        if (direction == "CUSTOMER_TO_DRIVER") {
            driverRatingPort.updateDriverRating(
                driverId = rateeId,
                newRating = request.stars
            )
        }

        return toResponse(savedRating)
    }

    fun getRatingsByDriver(
        driverId: Long
    ): List<RatingResponse> {

        return ratingRepository
            .findByRateeIdOrderByCreatedAtDesc(driverId)
            .filter { it.direction == "CUSTOMER_TO_DRIVER" }
            .map { rating ->
                toResponse(rating)
            }
    }

    private fun toResponse(
        rating: Rating
    ): RatingResponse {

        return RatingResponse(
            id = rating.id!!,
            bookingId = rating.bookingId,
            raterId = rating.raterId,
            rateeId = rating.rateeId,
            stars = rating.stars,
            tags = rating.tags,
            comment = rating.comment,
            direction = rating.direction,
            createdAt = rating.createdAt
        )
    }
}