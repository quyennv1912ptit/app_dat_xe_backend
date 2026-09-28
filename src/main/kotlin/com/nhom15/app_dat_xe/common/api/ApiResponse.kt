package com.nhom15.app_dat_xe.common.api

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Khuôn response chung cho mọi API.
 *
 * Thành công: { "success": true, "data": {...} }
 * Thất bại:   { "success": false, "error": { "code": "BOOKING_NOT_FOUND", "message": "..." } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<out T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)

        fun ok(): ApiResponse<Unit> = ApiResponse(success = true, data = Unit)

        fun fail(
            code: ErrorCode,
            message: String = code.defaultMessage,
            details: Map<String, String>? = null
        ): ApiResponse<Nothing> =
            ApiResponse(success = false, error = ApiError(code.name, message, details))
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiError(
    val code: String,
    val message: String,
    /** Chi tiết lỗi theo từng field khi validate, ví dụ { "phone": "Sai định dạng" } */
    val details: Map<String, String>? = null
)
