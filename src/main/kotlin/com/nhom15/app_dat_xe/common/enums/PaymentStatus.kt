package  com.nhom15.app_dat_xe.common.enums

/** Trạng thái thanh toán của một booking. */
enum class PaymentStatus {
    PENDING,   // chờ thanh toán
    PAID,      // đã thanh toán
    FAILED,    // thanh toán thất bại
    REFUNDED   // đã hoàn tiền
}
