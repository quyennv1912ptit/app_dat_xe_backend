package com.nhom15.app_dat_xe.account.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProfileResponse(
    val id: Long?,
    val uid: String?,
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: String?
)

data class UpdateProfileRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val fullName: String,
    @field:Email
    @field:Size(max = 255)
    val email: String? = null,
    @field:NotBlank
    @field:Size(min = 8, max = 20)
    val phoneNumber: String,
)
