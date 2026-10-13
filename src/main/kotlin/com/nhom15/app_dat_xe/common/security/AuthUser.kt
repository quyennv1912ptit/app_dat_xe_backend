package com.nhom15.app_dat_xe.common.security

import com.nhom15.app_dat_xe.common.enums.Role

/**
 * Người dùng đã đăng nhập, được FirebaseAuthFilter đặt vào SecurityContext sau khi xác thực token.
 *
 * Lưu ý: khách nằm ở bảng users, tài xế nằm ở bảng drivers, hai bảng có id riêng.
 * Vì vậy [id] chỉ có nghĩa khi đi kèm [role]: role = DRIVER thì id là Drivers.id, role = CUSTOMER thì id là Users.id.
 */
data class AuthUser(
    val id: Long,
    val role: Role,
    val firebaseUid: String = ""
)