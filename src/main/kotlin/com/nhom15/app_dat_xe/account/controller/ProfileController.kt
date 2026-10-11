package com.nhom15.app_dat_xe.account.controller

import com.nhom15.app_dat_xe.account.dto.ProfileResponse
import com.nhom15.app_dat_xe.account.dto.UpdateAvatarRequest
import com.nhom15.app_dat_xe.account.service.ProfileService
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val profileService: ProfileService
) {

    @GetMapping
    fun getProfile(@CurrentUser user: AuthUser): ApiResponse<ProfileResponse> =
        ApiResponse.ok(profileService.getProfile(user))

    @PutMapping("/avatar")
    fun updateAvatar(
        @CurrentUser user: AuthUser,
        @Valid @RequestBody request: UpdateAvatarRequest
    ): ApiResponse<ProfileResponse> =
        ApiResponse.ok(profileService.updateAvatar(user, request.avatarUrl))

    @PostMapping("/avatar/upload")
    fun uploadAvatar(
        @CurrentUser user: AuthUser,
        @RequestParam("file") file: MultipartFile
    ): ApiResponse<ProfileResponse> =
        ApiResponse.ok(profileService.uploadAvatar(user, file))
}