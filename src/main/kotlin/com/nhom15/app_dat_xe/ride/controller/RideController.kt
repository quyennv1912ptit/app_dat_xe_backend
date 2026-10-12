package com.nhom15.app_dat_xe.ride.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.api.PageResponse
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.ride.dto.request.CancelRideRequest
import com.nhom15.app_dat_xe.ride.dto.request.CreateRideRequest
import com.nhom15.app_dat_xe.ride.dto.request.EstimateRideRequest
import com.nhom15.app_dat_xe.ride.dto.request.RideListFilter
import com.nhom15.app_dat_xe.ride.dto.response.CreateRideResponse
import com.nhom15.app_dat_xe.ride.dto.response.EstimateRideResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideDetailResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideStatusResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideSummaryResponse
import com.nhom15.app_dat_xe.ride.service.RideEstimateService
import com.nhom15.app_dat_xe.ride.service.RideQueryService
import com.nhom15.app_dat_xe.ride.service.RideService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bookings")
class RideController(
    private val rideService: RideService,
    private val rideQueryService: RideQueryService,
    private val rideEstimateService: RideEstimateService,
) {
    @PostMapping("/estimate")
    fun estimateRide(
        @CurrentUser actor: AuthUser?,
        @Valid @RequestBody request: EstimateRideRequest,
    ): ApiResponse<EstimateRideResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideEstimateService.estimateRide(actor, request))
    }

    @GetMapping("/{id}")
    fun getRideDetail(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
    ): ApiResponse<RideDetailResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideQueryService.getDetail(actor, id))
    }

    @GetMapping
    fun listMyRides(
        @CurrentUser actor: AuthUser?,
        @Valid @ModelAttribute filter: RideListFilter,
    ): ApiResponse<PageResponse<RideSummaryResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideQueryService.listMyRides(actor, filter))
    }

    @PostMapping
    fun createRide(
        @CurrentUser actor: AuthUser,
        @Valid @RequestBody request: CreateRideRequest,
    ): ResponseEntity<ApiResponse<CreateRideResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(rideService.createRide(actor, request)))

    @PostMapping("/{id}/cancel")
    fun cancelRide(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: CancelRideRequest,
    ): ApiResponse<RideStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideService.cancelRide(actor, id, request))
    }
}
