package com.nhom15.app_dat_xe.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
class AvatarStorageService(
    @Value("\${app.upload.avatar-dir:uploads/avatars}")
    private val uploadDir: String
) {
    fun saveAvatar(file: MultipartFile): String {
        if (file.isEmpty) {
            throw IllegalArgumentException("Ảnh tải lên đang trống")
        }

        val contentType = file.contentType ?: ""

        if (!contentType.startsWith("image/")) {
            throw IllegalArgumentException("File tải lên phải là ảnh")
        }

        if (file.size > 5 * 1024 * 1024) {
            throw IllegalArgumentException("Ảnh không được vượt quá 5 MB")
        }

        val extension = when (contentType.lowercase()) {
            "image/jpeg", "image/jpg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            else -> throw IllegalArgumentException(
                "Chỉ hỗ trợ ảnh JPG, PNG, WEBP hoặc GIF"
            )
        }

        val directory = Path.of(uploadDir)
            .toAbsolutePath()
            .normalize()

        Files.createDirectories(directory)

        val fileName = UUID.randomUUID().toString() + extension
        val destination = directory.resolve(fileName).normalize()

        if (!destination.startsWith(directory)) {
            throw IllegalArgumentException("Đường dẫn file không hợp lệ")
        }

        file.inputStream.use { input ->
            Files.copy(input, destination)
        }

        return fileName
    }
}