package com.nhom15.app_dat_xe.auth.dto

import jakarta.validation.constraints.NotBlank

data class UpdateAvatarRequest(
    @field:NotBlank(message = "avatarUrl không được để trống")
    val avatarUrl: String
)
