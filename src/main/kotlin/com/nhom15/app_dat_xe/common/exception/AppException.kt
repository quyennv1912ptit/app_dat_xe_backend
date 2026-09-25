package com.nhom15.app_dat_xe.common.exception

import com.nhom15.app_dat_xe.common.api.ErrorCode
import org.springframework.http.HttpStatus

/**
 * Class cha của mọi lỗi nghiệp vụ. GlobalExceptionHandler bắt class này
 * và trả về ApiResponse với đúng HTTP status.
 */
open class AppException(
    val errorCode: ErrorCode,
    message: String = errorCode.defaultMessage,
    val httpStatus: HttpStatus = errorCode.httpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)
