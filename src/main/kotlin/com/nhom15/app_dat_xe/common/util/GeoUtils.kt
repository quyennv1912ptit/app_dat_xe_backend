package com.nhom15.app_dat_xe.common.util

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    private const val EARTH_RADIUS_KM = 6371.0088

    /**
     * Khoảng cách đường chim bay (haversine) giữa hai điểm, đơn vị km.
     * Đây không phải quãng đường lái xe thực tế. Giá cước nên dùng quãng đường
     * từ dịch vụ bản đồ, còn hàm này dùng để ước lượng và lọc tài xế gần.
     */
    fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(min(1.0, sqrt(h)))
    }

    fun isValid(p: GeoPoint): Boolean = p.lat in -90.0..90.0 && p.lng in -180.0..180.0
}
