package com.nhom15.app_dat_xe.vehicle.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.vehicle.dto.CreateVehicleRequest
import com.nhom15.app_dat_xe.vehicle.dto.VehicleResponse
import com.nhom15.app_dat_xe.vehicle.service.VehicleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/vehicles")
@PreAuthorize("hasRole('CUSTOMER')")
class VehicleController(private val vehicleService: VehicleService) {
    @GetMapping
    fun list(@CurrentUser actor: AuthUser): ApiResponse<List<VehicleResponse>> =
        ApiResponse.ok(vehicleService.list(actor))

    @PostMapping
    fun create(
        @CurrentUser actor: AuthUser,
        @Valid @RequestBody request: CreateVehicleRequest,
    ): ResponseEntity<ApiResponse<VehicleResponse>> = ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.ok(vehicleService.create(actor, request)))

    @DeleteMapping("/{id}")
    fun delete(@CurrentUser actor: AuthUser, @PathVariable id: Long): ApiResponse<Unit> {
        vehicleService.delete(actor, id)
        return ApiResponse.ok()
    }
}
