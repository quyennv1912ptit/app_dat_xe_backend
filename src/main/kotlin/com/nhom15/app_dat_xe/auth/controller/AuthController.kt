package com.nhom15.app_dat_xe.auth.controller

import com.nhom15.app_dat_xe.auth.dto.LinkPhoneRequest
import com.nhom15.app_dat_xe.auth.dto.LoginRequest
import com.nhom15.app_dat_xe.auth.dto.LoginResponse
import com.nhom15.app_dat_xe.auth.dto.RegisterRequest
import com.nhom15.app_dat_xe.auth.service.AuthService
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @GetMapping("/test")
    fun test(): ApiResponse<String> = ApiResponse.ok("BACKEND OK")

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(authService.login(idTokenOf(request), request.role))

    @PostMapping("/facebook")
    fun facebookLogin(@RequestBody request: LoginRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(authService.loginWithFacebook(idTokenOf(request), request.role))

    @PostMapping("/google")
    fun googleLogin(@RequestBody request: LoginRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(authService.loginWithGoogle(idTokenOf(request), request.role))

    @PostMapping("/link-phone")
    fun linkPhone(@RequestBody request: LinkPhoneRequest): ApiResponse<LoginResponse> {
        val providerIdToken = requireNotBlank(request.providerIdToken, "Provider ID Token không được để trống")
        val phoneIdToken = requireNotBlank(request.phoneIdToken, "Phone ID Token không được để trống")
        val phoneNumber = requireNotBlank(request.phoneNumber, "Số điện thoại không được để trống")
        return ApiResponse.ok(authService.linkPhone(providerIdToken, phoneIdToken, phoneNumber, request.role))
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ApiResponse<LoginResponse> {
        val idToken = requireNotBlank(request.idToken, "Firebase ID Token không được để trống")
        val phoneNumber = requireNotBlank(request.phoneNumber, "Số điện thoại không được để trống")
        val fullName = requireNotBlank(request.fullName, "Họ tên không được để trống")
        val role = requireNotBlank(request.role, "Role phải là CUSTOMER hoặc DRIVER")
        return ApiResponse.ok(authService.register(idToken, phoneNumber, fullName, request.email, role))
    }

    private fun idTokenOf(request: LoginRequest): String =
        requireNotBlank(request.idToken, "Firebase ID Token không được để trống")

    private fun requireNotBlank(value: String?, message: String): String {
        if (value.isNullOrBlank()) {
            throw BadRequestException(ErrorCode.VALIDATION_ERROR, message)
        }
        return value
    }
}