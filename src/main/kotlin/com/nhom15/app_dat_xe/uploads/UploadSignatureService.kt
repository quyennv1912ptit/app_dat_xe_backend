package com.nhom15.app_dat_xe.uploads

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.nhom15.app_dat_xe.common.security.AuthUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UploadSignatureService(
    private val cloudinary: Cloudinary,
    @Value("\${cloudinary.cloud-name}") private val cloudName: String,
    @Value("\${cloudinary.api-key}") private val apiKey: String,
    @Value("\${cloudinary.api-secret}") private val apiSecret: String,
) {
    fun create(actor: AuthUser): UploadSignatureResponse {
        val timestamp = Instant.now().epochSecond
        val folder = "app_dat_xe/${actor.role.name.lowercase()}/${actor.id}"
        val params = mapOf<String, Any>(
            "folder" to folder,
            "timestamp" to timestamp
        )
        return UploadSignatureResponse(
            signature = cloudinary.apiSignRequest(params, apiSecret),
            timestamp = timestamp,
            apiKey = apiKey,
            cloudName = cloudName,
            folder = folder,
        )
    }
}
