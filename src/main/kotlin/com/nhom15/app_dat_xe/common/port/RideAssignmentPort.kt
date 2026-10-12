package com.nhom15.app_dat_xe.common.port

/**
 * Ride-core (Người 2A) cài đặt. Matching (Người 2B) gọi khi tài xế bấm nhận chuyến.
 * Chỉ gán khi ride còn SEARCHING bằng một UPDATE có điều kiện.
 * Bên gọi kiểm tra quyền nhận offer, hạn offer và điều kiện của tài xế.
 */
interface RideAssignmentPort {
    /**
     * Ride ghi lịch sử và phát RideDriverAssigned khi gán thành công; Matching dùng kết quả để phản hồi.
     * Trả null nếu ride không tồn tại hoặc không còn SEARCHING, kể cả gọi lại
     * với cùng tài xế. ID không dương gây BadRequestException; lỗi kỹ thuật truyền lên.
     *
     * Ride/history tham gia giao dịch của bên gọi nếu có. Result chưa bảo đảm
     * giao dịch ngoài đã commit; side effect nên xử lý sau commit.
     * UPDATE flush rồi clear persistence context: bên gọi cần nạp lại hoặc lưu
     * tường minh các entity đã nạp trước đó, không dựa vào dirty checking cũ.
     * History hiện ghi actorId = driverId; chưa ghi được admin thao tác.
     */
    fun assignDriver(rideId: Long, driverId: Long): RideAssignmentResult?
}

data class RideAssignmentResult(
    val rideId: Long,
    val customerId: Long,
    val driverId: Long,
)
