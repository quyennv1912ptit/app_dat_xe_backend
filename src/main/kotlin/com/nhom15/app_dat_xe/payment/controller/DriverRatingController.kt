
package com.nhom15.app_dat_xe.payment.controller

import com.nhom15.app_dat_xe.payment.dto.RatingResponse
import com.nhom15.app_dat_xe.payment.service.RatingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/drivers")
class DriverRatingController(
    private val ratingService: RatingService
) {

    @GetMapping("/{driverId}/ratings")
    fun getRatingsByDriver(
        @PathVariable driverId: Long
    ): ResponseEntity<List<RatingResponse>> {

        val ratings = ratingService.getRatingsByDriver(driverId)

        return ResponseEntity.ok(ratings)
    }
}