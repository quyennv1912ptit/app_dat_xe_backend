package com.nhom15.app_dat_xe.common.exception

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    /** Lỗi nghiệp vụ do chính mình ném ra. */
    @ExceptionHandler(AppException::class)
    fun handleApp(e: AppException): ResponseEntity<ApiResponse<Nothing>> {
        if (e.httpStatus.is5xxServerError) log.error("AppException: {}", e.message, e)
        else log.warn("AppException [{}]: {}", e.errorCode, e.message)
        return respond(e.httpStatus, e.errorCode, e.message ?: e.errorCode.defaultMessage)
    }

    /** @Valid trên @RequestBody. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBodyValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val details = e.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Không hợp lệ") }
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, details = details)
    }

    /** @Validated trên @RequestParam / @PathVariable. */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraint(e: ConstraintViolationException): ResponseEntity<ApiResponse<Nothing>> {
        val details = e.constraintViolations
            .associate { it.propertyPath.toString() to it.message }
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, details = details)
    }

    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class
    )
    fun handleMalformed(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Bad request: {}", e.message)
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Yêu cầu không hợp lệ hoặc sai định dạng")
    }

    /** @PreAuthorize ném ra khi sai role. */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> =
        respond(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN)

    /** Hai người cùng sửa một bản ghi có @Version (ví dụ Booking, Wallet). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(e: ObjectOptimisticLockingFailureException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Optimistic lock conflict: {}", e.message)
        return respond(HttpStatus.CONFLICT, ErrorCode.CONFLICT)
    }

    /** Lỗi 4xx có sẵn của Spring MVC (405, 415, 404 static...) giữ nguyên status. */
    @ExceptionHandler(ErrorResponse::class)
    fun handleSpringError(e: ErrorResponse): ResponseEntity<ApiResponse<Nothing>> {
        val status = e.statusCode
        val code = if (status.value() == 404) ErrorCode.NOT_FOUND else ErrorCode.BAD_REQUEST
        return ResponseEntity.status(status)
            .body(ApiResponse.fail(code, e.body.detail ?: code.defaultMessage))
    }

    /** Lưới an toàn cuối cùng: không lộ stacktrace ra ngoài. */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception", e)
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR)
    }

    private fun respond(
        status: HttpStatus,
        code: ErrorCode,
        message: String = code.defaultMessage,
        details: Map<String, String>? = null
    ): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(status).body(ApiResponse.fail(code, message, details))
}
