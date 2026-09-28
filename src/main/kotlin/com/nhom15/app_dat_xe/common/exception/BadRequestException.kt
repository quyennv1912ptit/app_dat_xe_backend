package com.nhom15.app_dat_xe.common.exception

import com.nhom15.app_dat_xe.common.api.ErrorCode
import org.springframework.http.HttpStatus

/** 400: dữ liệu gửi lên sai theo luật nghiệp vụ (không phải lỗi validate annotation). */
class BadRequestException(
    errorCode: ErrorCode = ErrorCode.BAD_REQUEST,
    message: String = errorCode.defaultMessage
) : AppException(errorCode, message, HttpStatus.BAD_REQUEST)
