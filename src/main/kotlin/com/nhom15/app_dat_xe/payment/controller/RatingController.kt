
package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.payment.dto.CreateRatingRequest
import com.nhom15.app_dat_xe.payment.dto.RatingResponse
import com.nhom15.app_dat_xe.payment.service.RatingService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/bookings")
class RatingController(
    private val ratingService: RatingService
) {
    @PostMapping("/{bookingId}/ratings")
    fun createRating(
        @PathVariable bookingId: Long,
        @CurrentUser currentUser: AuthUser,
        @Valid @RequestBody request: CreateRatingRequest
    ): ResponseEntity<RatingResponse> {
        val ratingRequest = request.copy(bookingId = bookingId)

        val rating = ratingService.createRating(
            request = ratingRequest,
            raterId = currentUser.id
        )

        return ResponseEntity.ok(rating)
    }
}