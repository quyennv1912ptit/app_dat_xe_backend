package com.nhom15.app_dat_xe.auth.service

import com.nhom15.app_dat_xe.auth.dto.ProfileResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.security.AuthUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

/** Người dùng lấy từ [AuthUser] (do filter đặt), service không tự verify token nữa. */
@Service
class ProfileService(
    private val accountService: AccountService,
    private val avatarStorageService: AvatarStorageService
) {

    @Transactional(readOnly = true)
    fun getProfile(user: AuthUser): ProfileResponse =
        accountService.getById(user.userId, user.role).toProfileResponse()

    @Transactional
    fun updateAvatar(user: AuthUser, avatarUrl: String): ProfileResponse {
        val url = avatarUrl.trim()
        if (!(url.startsWith("http://") || url.startsWith("https://")) || url.length > MAX_URL_LENGTH) {
            throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Đường dẫn ảnh không hợp lệ")
        }
        return accountService.updateAvatar(user.userId, user.role, url).toProfileResponse()
    }

    @Transactional
    fun uploadAvatar(user: AuthUser, file: MultipartFile): ProfileResponse {
        val url = avatarStorageService.saveAvatar(file)
        return accountService.updateAvatar(user.userId, user.role, url).toProfileResponse()
    }

    private fun Account.toProfileResponse() =
        ProfileResponse(id, firebaseUid, fullName, email, phoneNumber, avatarUrl, role.name)

    private companion object {
        const val MAX_URL_LENGTH = 500
    }
}