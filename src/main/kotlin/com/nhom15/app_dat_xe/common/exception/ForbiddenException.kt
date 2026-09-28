package com.nhom15.app_dat_xe.common.exception

import com.nhom15.app_dat_xe.common.api.ErrorCode
import org.springframework.http.HttpStatus

/** 403: đã đăng nhập nhưng không có quyền (ví dụ khách xem booking của người khác). */
class ForbiddenException(
    errorCode: ErrorCode = ErrorCode.FORBIDDEN,
    message: String = errorCode.defaultMessage
) : AppException(errorCode, message, HttpStatus.FORBIDDEN)
