package com.nhom15.app_dat_xe.common.port

/**
 * Booking-core (Người 2A) cài đặt. Matching (Người 2B) gọi khi tài xế bấm nhận chuyến.
 * Cài đặt phải atomic, ví dụ:
 *   UPDATE booking SET driver_id = ?, status = 'DRIVER_ASSIGNED'
 *   WHERE id = ? AND status = 'SEARCHING'
 */
interface BookingAssignmentPort {
    /** true nếu thắng (booking vẫn còn SEARCHING), false nếu đã có người khác nhận hoặc đã hủy. */
    fun assignDriver(bookingId: Long, driverId: Long): Boolean
}
