package com.nhom15.app_dat_xe.tracking.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.enums.DriverStatus
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.tracking.service.DriverPresenceService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class DriverAvailabilityRequest(val online: Boolean)

data class DriverAvailabilityResponse(val status: DriverStatus)

@RestController
@RequestMapping("/driver/me")
@PreAuthorize("hasRole('DRIVER')")
class DriverController(
    private val driverPresenceService: DriverPresenceService,
) {

    @PutMapping("/availability")
    fun setAvailability(
        @CurrentUser user: AuthUser,
        @RequestBody request: DriverAvailabilityRequest,
    ): ApiResponse<DriverAvailabilityResponse> =
        ApiResponse.ok(
            DriverAvailabilityResponse(
                driverPresenceService.setOnline(user.id, request.online),
            ),
        )
}
