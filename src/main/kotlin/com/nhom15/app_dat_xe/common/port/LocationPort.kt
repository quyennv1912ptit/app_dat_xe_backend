package com.nhom15.app_dat_xe.common.port

import com.nhom15.app_dat_xe.common.domain.GeoPoint

/**
 * Realtime (Người 3) cài đặt bằng Redis GEO. Matching (Người 2B) gọi qua interface này.
 * Trong lúc chờ có thể viết FakeLocationPort trả dữ liệu giả để test.
 */
interface LocationPort {
    /** Tài xế đang ONLINE trong bán kính [radiusKm] quanh [center], gần nhất trước, tối đa [limit]. */
    fun findNearby(center: GeoPoint, radiusKm: Double, limit: Int): List<NearbyDriver>
}

data class NearbyDriver(
    val driverId: Long,
    val distanceKm: Double
)
