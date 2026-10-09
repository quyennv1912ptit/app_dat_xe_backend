package com.nhom15.app_dat_xe.common.port

/**
 * Booking-core (Người 2A) cài đặt. Matching (Người 2B) gọi khi tài xế bấm nhận chuyến.
 * Chỉ gán khi booking còn SEARCHING bằng một UPDATE có điều kiện.
 * Bên gọi kiểm tra quyền nhận offer, hạn offer và điều kiện của tài xế.
 */
interface BookingAssignmentPort {
    /**
     * Trả dữ liệu để Matching phát DriverAssigned khi gán thành công.
     * Trả null nếu booking không tồn tại hoặc không còn SEARCHING, kể cả gọi lại
     * với cùng tài xế. ID không dương gây BadRequestException; lỗi kỹ thuật truyền lên.
     *
     * Booking/history tham gia giao dịch của bên gọi nếu có. Result chưa bảo đảm
     * giao dịch ngoài đã commit; side effect nên xử lý sau commit.
     * UPDATE flush rồi clear persistence context: bên gọi cần nạp lại hoặc lưu
     * tường minh các entity đã nạp trước đó, không dựa vào dirty checking cũ.
     * History hiện ghi actorId = driverId; chưa ghi được admin thao tác.
     */
    fun assignDriver(bookingId: Long, driverId: Long): BookingAssignmentResult?
}

data class BookingAssignmentResult(
    val bookingId: Long,
    val customerId: Long,
    val driverId: Long,
)
