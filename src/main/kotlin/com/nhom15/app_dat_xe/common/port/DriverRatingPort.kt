// Cung cấp chức năng cập nhật điểm đánh giá trung bình của tài xế.

package com.nhom15.app_dat_xe.common.port

interface DriverRatingPort {

    fun updateDriverRating(
        driverId: Long,
        newRating: Int
    )
}