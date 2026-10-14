package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.account.repository.DriverRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.ride.dto.request.CreateRideRatingRequest
import com.nhom15.app_dat_xe.ride.dto.request.RideRatingResponse
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RideRatingService(
    private val rideRepository: RideRepository,
    private val driverRepository: DriverRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Transactional
    fun rate(actor: AuthUser, rideId: Long, request: CreateRideRatingRequest): RideRatingResponse {
        if (actor.role != Role.CUSTOMER) throw ForbiddenException(message = "Chỉ khách hàng được đánh giá chuyến")
        if (rideId <= 0) throw BadRequestException(message = "rideId phải lớn hơn 0")
        val ride = rideRepository.findById(rideId).orElseThrow { NotFoundException(ErrorCode.RIDE_NOT_FOUND) }
        if (ride.customerId != actor.id) throw ForbiddenException(message = "Bạn không sở hữu chuyến này")
        if (ride.status != RideStatus.COMPLETED) {
            throw BadRequestException(message = "Chỉ đánh giá được chuyến đã hoàn thành")
        }
        val driverId = ride.driverId ?: throw BadRequestException(message = "Chuyến chưa có tài xế")
        if (request.stars !in 1..5 || request.tip < 0 || (request.comment?.length ?: 0) > 2000) {
            throw BadRequestException(message = "Thông tin đánh giá không hợp lệ")
        }
        try {
            jdbcTemplate.update(
                """INSERT INTO ride_ratings (ride_id, customer_id, driver_id, stars, comment, tip)
                   VALUES (?, ?, ?, ?, ?, ?)""".trimIndent(),
                rideId, actor.id, driverId, request.stars, request.comment?.trim()?.takeIf(String::isNotEmpty), request.tip,
            )
        } catch (_: DataIntegrityViolationException) {
            throw ConflictException(ErrorCode.RATING_ALREADY_EXISTS)
        }
        val average = jdbcTemplate.queryForObject(
            "SELECT AVG(stars)::double precision FROM ride_ratings WHERE driver_id = ?",
            Double::class.java,
            driverId,
        ) ?: request.stars.toDouble()
        val driver = driverRepository.findById(driverId).orElseThrow { NotFoundException(ErrorCode.USER_NOT_FOUND) }
        driver.averageRating = average
        driverRepository.saveAndFlush(driver)
        return RideRatingResponse(rideId, driverId, request.stars, request.comment?.trim(), request.tip)
    }
}
