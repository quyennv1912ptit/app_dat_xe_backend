package com.nhom15.app_dat_xe.matching.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.matching.service.RideAcceptanceService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/offers")
class OfferController (
    private val acceptanceService: RideAcceptanceService
) {
    @PostMapping("/{rideId}/accept")
    fun driverAcceptedOffer(@PathVariable rideId: Long, @CurrentUser user: AuthUser) : ApiResponse<String> {
        if (acceptanceService.accept(rideId, user.id)) {
            return ApiResponse.ok("Nhận chuyến thành công")
        } else {
            throw ConflictException(message = "lời mời đã hết hạn hoặc chuyến đã giao người khác.")
        }
    }

    @PostMapping("/{rideId}/reject")
    fun driverRejectedOffer(@PathVariable rideId: Long, @CurrentUser user: AuthUser): ApiResponse<Unit> {
        acceptanceService.reject(rideId, user.id)
        return ApiResponse.ok()
    }
}