package com.nhom15.app_dat_xe.common.exception

import com.nhom15.app_dat_xe.common.api.ErrorCode
import org.springframework.http.HttpStatus

/**
 * 409: xung đột trạng thái. Dùng cho tranh chấp nhận chuyến
 * (BOOKING_ALREADY_ASSIGNED), sai trạng thái (INVALID_BOOKING_STATE), thanh toán trùng...
 */
class ConflictException(
    errorCode: ErrorCode = ErrorCode.CONFLICT,
    message: String = errorCode.defaultMessage
) : AppException(errorCode, message, HttpStatus.CONFLICT)
