package com.nhom15.app_dat_xe.common.exception

import com.nhom15.app_dat_xe.common.api.ErrorCode
import org.springframework.http.HttpStatus

/** 404: không tìm thấy tài nguyên. Ví dụ: throw NotFoundException(ErrorCode.BOOKING_NOT_FOUND) */
class NotFoundException(
    errorCode: ErrorCode = ErrorCode.NOT_FOUND,
    message: String = errorCode.defaultMessage
) : AppException(errorCode, message, HttpStatus.NOT_FOUND)
