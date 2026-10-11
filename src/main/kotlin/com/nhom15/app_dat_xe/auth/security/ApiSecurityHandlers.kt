package com.nhom15.app_dat_xe.auth.security

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.AppException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/**
 * Lỗi sinh ra trong security filter không đi qua GlobalExceptionHandler,
 * nên phải tự ghi JSON theo khuôn ApiResponse.
 */
@Component
class ApiAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val error = request.getAttribute(FirebaseAuthFilter.AUTH_ERROR_ATTR) as? AppException
        val status = error?.httpStatus ?: HttpStatus.UNAUTHORIZED
        val body = if (error != null) {
            ApiResponse.fail(error.errorCode, error.message ?: error.errorCode.defaultMessage)
        } else {
            ApiResponse.fail(ErrorCode.UNAUTHORIZED)
        }
        writeJson(objectMapper, response, status, body)
    }
}

@Component
class ApiAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        writeJson(objectMapper, response, HttpStatus.FORBIDDEN, ApiResponse.fail(ErrorCode.FORBIDDEN))
    }
}

private fun writeJson(
    objectMapper: ObjectMapper,
    response: HttpServletResponse,
    status: HttpStatus,
    body: ApiResponse<Nothing>
) {
    response.status = status.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.characterEncoding = "UTF-8"
    objectMapper.writeValue(response.writer, body)
}
