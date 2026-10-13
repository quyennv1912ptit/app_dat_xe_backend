package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.ride.dto.internal.RideQuote
import com.nhom15.app_dat_xe.ride.dto.internal.RideQuoteInput

/** Tính giá theo tọa độ và thời điểm, dùng chung cho tạo ride và báo giá. */
fun interface RideQuoteProvider {
    fun quote(input: RideQuoteInput): RideQuote
}
