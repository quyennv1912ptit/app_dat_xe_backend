package com.nhom15.app_dat_xe.ride.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.ride.dto.request.CreateRideRatingRequest
import com.nhom15.app_dat_xe.ride.dto.request.RideRatingResponse
import com.nhom15.app_dat_xe.ride.service.RideRatingService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rides")
class RideRatingController(private val ratingService: RideRatingService) {
    @PostMapping("/{id}/rating")
    fun rate(
        @CurrentUser actor: AuthUser,
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateRideRatingRequest,
    ): ApiResponse<RideRatingResponse> = ApiResponse.ok(ratingService.rate(actor, id, request))
}
