package com.nhom15.app_dat_xe.auth.controller

import com.nhom15.app_dat_xe.auth.dto.ProfileResponse
import com.nhom15.app_dat_xe.auth.service.ProfileService
import org.springframework.web.bind.annotation.*
import com.nhom15.app_dat_xe.auth.service.AvatarStorageService
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val profileService: ProfileService,
    private val avatarStorageService: AvatarStorageService
) {
    @GetMapping
    fun getProfile(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam role: String
    ): ProfileResponse {
        val idToken = authorization.removePrefix("Bearer ")
        return profileService.getProfile(
            idToken,
            role
        )
    }

    @PutMapping("/avatar")
    fun updateAvatar(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam role: String,
        @RequestBody request: UpdateAvatarRequest
    ): ProfileResponse {

        val idToken = authorization.removePrefix("Bearer ")

        return profileService.updateAvatar(
            idToken,
            role,
            request.avatarUrl
        )
    }
    @PostMapping("/avatar/upload")
    fun uploadAvatar(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam role: String,
        @RequestParam("file") file: MultipartFile
    ): ProfileResponse {

        val idToken = authorization.removePrefix("Bearer ")

        val fileName = avatarStorageService.saveAvatar(file)

        val avatarUrl = "http://10.0.2.2:8080/uploads/avatars/$fileName"

        return profileService.updateAvatar(
            idToken,
            role,
            avatarUrl
        )
    }
}

data class UpdateAvatarRequest(
    val avatarUrl: String
)