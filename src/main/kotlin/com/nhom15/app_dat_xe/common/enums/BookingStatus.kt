package  com.nhom15.app_dat_xe.common.enums


/**
 * Trạng thái của một chuyến thuê lái xe hộ.
 * Chỉ khai báo giá trị. Luật chuyển trạng thái nằm ở booking-core (BookingStateMachine).
 *
 * Luồng chính:
 * PENDING -> SEARCHING -> DRIVER_ASSIGNED -> DRIVER_ARRIVING -> DRIVER_ARRIVED
 * -> VEHICLE_HANDOVER -> IN_PROGRESS -> ARRIVED_DESTINATION -> VEHICLE_RETURNED -> COMPLETED
 */
enum class BookingStatus {
    PENDING,             // vừa tạo, chưa bắt đầu tìm tài xế (hoặc booking đặt lịch chưa đến giờ)
    SEARCHING,           // đang tìm tài xế
    DRIVER_ASSIGNED,     // đã có tài xế nhận chuyến
    DRIVER_ARRIVING,     // tài xế đang đến điểm đón
    DRIVER_ARRIVED,      // tài xế đã đến điểm đón
    VEHICLE_HANDOVER,    // đang kiểm tra và bàn giao xe (chụp ảnh, ghi số km)
    IN_PROGRESS,         // đang chạy
    ARRIVED_DESTINATION, // đã đến điểm đến
    VEHICLE_RETURNED,    // đã bàn giao xe lại cho khách
    COMPLETED,           // hoàn tất chuyến

    // Nhánh kết thúc bất thường
    CANCELLED,           // bị hủy (bởi khách, tài xế hoặc admin)
    NO_DRIVER_FOUND,     // hết các vòng tìm mà không có tài xế
    EXPIRED              // quá hạn (ví dụ booking đặt lịch không ai nhận)
}
