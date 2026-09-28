package com.nhom15.app_dat_xe.common.security

import com.nhom15.app_dat_xe.common.enums.Role

/**
 * Thông tin người dùng đang đăng nhập.
 * Module auth (Người 1) phải đặt đối tượng này làm `principal` của Authentication
 * trong JWT filter, để các controller lấy được bằng @CurrentUser.
 */
data class AuthUser(
    val userId: Long,
    val role: Role
)
