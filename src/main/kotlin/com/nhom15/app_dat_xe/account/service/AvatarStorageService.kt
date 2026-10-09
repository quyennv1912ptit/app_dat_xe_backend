package com.nhom15.app_dat_xe.account.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AvatarStorageService(
    private val cloudinary: Cloudinary
) {
    fun saveAvatar(file: MultipartFile): String {
        if (file.isEmpty) {
            throw invalid("Ảnh tải lên đang trống")
        }

        if (file.size > MAX_SIZE_BYTES) {
            throw invalid("Ảnh không được vượt quá 5 MB")
        }

        when (file.contentType?.lowercase()) {
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif" -> {}
            else -> throw invalid("Chỉ hỗ trợ ảnh JPG, PNG, WEBP hoặc GIF")
        }

        try {
            val uploadResult = cloudinary.uploader().upload(
                file.bytes,
                ObjectUtils.asMap(
                    "folder", "app_dat_xe/avatars",
                    "resource_type", "image"
                )
            )

            return uploadResult["secure_url"] as String

        } catch (e: Exception) {
            throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Lỗi khi upload ảnh: ${e.message}")
        }
    }

    private fun invalid(message: String) = BadRequestException(ErrorCode.VALIDATION_ERROR, message)

    private companion object {
        const val MAX_SIZE_BYTES = 5L * 1024 * 1024 // 5 MB
    }
}