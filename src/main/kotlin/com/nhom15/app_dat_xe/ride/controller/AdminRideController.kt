package com.nhom15.app_dat_xe.ride.controller

import com.nhom15.app_dat_xe.ride.dto.request.RideListFilter
import com.nhom15.app_dat_xe.ride.dto.response.RideSummaryResponse
import com.nhom15.app_dat_xe.ride.service.RideQueryService
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.api.PageResponse
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/rides")
class AdminRideController(
    private val rideQueryService: RideQueryService,
) {
    @GetMapping
    fun listRides(
        @CurrentUser actor: AuthUser?,
        @Valid @ModelAttribute filter: RideListFilter,
    ): ApiResponse<PageResponse<RideSummaryResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(rideQueryService.listAdminRides(actor, filter))
    }
}
