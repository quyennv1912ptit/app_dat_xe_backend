package com.nhom15.app_dat_xe.admin.controller

import com.nhom15.app_dat_xe.admin.dto.ReviewDriverLicenseRequest
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.tracking.dto.DriverProfileResponse
import com.nhom15.app_dat_xe.tracking.service.DriverProfileService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PutMapping

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(private val driverProfileService: DriverProfileService) {
    @PutMapping("/drivers/{id}/license-review")
    fun reviewDriverLicense(
        @PathVariable id: Long,
        @RequestBody request: ReviewDriverLicenseRequest,
    ): ApiResponse<DriverProfileResponse> =
        ApiResponse.ok(driverProfileService.review(id, request.approved))

}
