package com.nhom15.app_dat_xe.auth.service

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
class AvatarStorageService(
    @Value("\${app.upload.avatar-dir:uploads/avatars}")
    private val uploadDir: String,
    /** Địa chỉ app truy cập được tới backend. 10.0.2.2 chỉ đúng với Android emulator. */
    @Value("\${app.public-base-url:http://10.0.2.2:8080}")
    private val publicBaseUrl: String
) {

    /** Lưu file và trả về URL công khai của ảnh. */
    fun saveAvatar(file: MultipartFile): String {
        if (file.isEmpty) {
            throw invalid("Ảnh tải lên đang trống")
        }
        if (file.size > MAX_SIZE_BYTES) {
            throw invalid("Ảnh không được vượt quá 5 MB")
        }

        val extension = when (file.contentType?.lowercase()) {
            "image/jpeg", "image/jpg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            else -> throw invalid("Chỉ hỗ trợ ảnh JPG, PNG, WEBP hoặc GIF")
        }

        val directory = Path.of(uploadDir).toAbsolutePath().normalize()
        Files.createDirectories(directory)

        val fileName = UUID.randomUUID().toString() + extension
        val destination = directory.resolve(fileName).normalize()
        if (!destination.startsWith(directory)) {
            throw invalid("Đường dẫn file không hợp lệ")
        }

        file.inputStream.use { Files.copy(it, destination) }

        return "${publicBaseUrl.trimEnd('/')}/uploads/avatars/$fileName"
    }

    private fun invalid(message: String) = BadRequestException(ErrorCode.VALIDATION_ERROR, message)

    private companion object {
        const val MAX_SIZE_BYTES = 5L * 1024 * 1024
    }
}