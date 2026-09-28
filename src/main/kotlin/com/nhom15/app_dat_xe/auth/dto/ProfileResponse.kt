package com.nhom15.app_dat_xe.auth.dto

data class ProfileResponse(
    val id: Long?,
    val uid: String?,
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    val role: String?
)