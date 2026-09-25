package com.nhom15.app_dat_xe.common.api

import org.springframework.http.HttpStatus

/**
 * Mã lỗi dùng chung toàn hệ thống. Mỗi mã gắn sẵn HTTP status và message mặc định.
 * Module nào cần mã lỗi mới thì thêm vào đúng nhóm bên dưới và báo cả nhóm.
 */
enum class ErrorCode(val httpStatus: HttpStatus, val defaultMessage: String) {

    // ---- Chung ----
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập hoặc phiên đã hết hạn"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    CONFLICT(HttpStatus.CONFLICT, "Dữ liệu vừa bị thay đổi, vui lòng thử lại"),

    // ---- Auth & User ----
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Số điện thoại đã được đăng ký"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Số điện thoại hoặc mật khẩu không đúng"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Mã OTP không đúng"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    DRIVER_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Tài xế chưa được duyệt"),
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy xe"),

    // ---- Booking ----
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến đi"),
    INVALID_BOOKING_STATE(HttpStatus.CONFLICT, "Trạng thái chuyến đi không cho phép thao tác này"),
    BOOKING_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "Chuyến đã có tài xế nhận"),
    BOOKING_CANNOT_CANCEL(HttpStatus.CONFLICT, "Không thể hủy chuyến ở trạng thái hiện tại"),
    HANDOVER_REPORT_REQUIRED(HttpStatus.BAD_REQUEST, "Cần hoàn tất biên bản bàn giao xe"),
    PRICING_RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "Chưa có bảng giá áp dụng"),

    // ---- Matching ----
    OFFER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy lời mời nhận chuyến"),
    OFFER_EXPIRED(HttpStatus.CONFLICT, "Lời mời nhận chuyến đã hết hạn"),
    DRIVER_NOT_AVAILABLE(HttpStatus.CONFLICT, "Tài xế không sẵn sàng nhận chuyến"),

    // ---- Realtime ----
    LOCATION_UNAVAILABLE(HttpStatus.NOT_FOUND, "Chưa có vị trí của tài xế"),

    // ---- Payment & Rating ----
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch thanh toán"),
    PAYMENT_ALREADY_PAID(HttpStatus.CONFLICT, "Chuyến đã được thanh toán"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Số dư ví không đủ"),
    INVALID_SIGNATURE(HttpStatus.BAD_REQUEST, "Chữ ký callback không hợp lệ"),
    VOUCHER_INVALID(HttpStatus.BAD_REQUEST, "Mã giảm giá không hợp lệ hoặc đã hết hạn"),
    RATING_ALREADY_EXISTS(HttpStatus.CONFLICT, "Bạn đã đánh giá chuyến này rồi")
}
