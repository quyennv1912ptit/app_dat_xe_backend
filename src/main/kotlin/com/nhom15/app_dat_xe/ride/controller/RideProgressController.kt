package com.nhom15.app_dat_xe.ride.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.ride.dto.request.ArriveDestinationRequest
import com.nhom15.app_dat_xe.ride.dto.request.CompleteRideRequest
import com.nhom15.app_dat_xe.ride.dto.request.DriverArrivedRequest
import com.nhom15.app_dat_xe.ride.dto.request.DriverStartArrivingRequest
import com.nhom15.app_dat_xe.ride.dto.request.StartTripRequest
import com.nhom15.app_dat_xe.ride.dto.response.RideStatusResponse
import com.nhom15.app_dat_xe.ride.service.RideProgressService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rides")
class RideProgressController(
    private val rideProgressService: RideProgressService,
) {
    @PostMapping("/{id}/start-arriving")
    fun startArriving(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: DriverStartArrivingRequest,
    ): ApiResponse<RideStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideProgressService.startArriving(actor, id, request))
    }

    @PostMapping("/{id}/arrived")
    fun arrivePickup(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: DriverArrivedRequest,
    ): ApiResponse<RideStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideProgressService.arrivePickup(actor, id, request))
    }

    @PostMapping("/{id}/start")
    fun startTrip(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: StartTripRequest,
    ): ApiResponse<RideStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideProgressService.startTrip(actor, id, request))
    }

    @PostMapping("/{id}/arrive-destination")
    fun arriveDestination(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: ArriveDestinationRequest,
    ): ApiResponse<RideStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideProgressService.arriveDestination(actor, id, request))
    }

    @PostMapping("/{id}/complete")
    fun completeRide(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: CompleteRideRequest,
    ): ApiResponse<RideStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideProgressService.completeRide(actor, id, request))
    }
}
