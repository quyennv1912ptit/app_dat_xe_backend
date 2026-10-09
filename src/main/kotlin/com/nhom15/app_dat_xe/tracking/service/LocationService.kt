package com.nhom15.app_dat_xe.tracking.service

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class LocationService(private val redisTemplate: StringRedisTemplate) {

    companion object {
        private const val GEO_KEY = "available_drivers"
        private const val ALIVE_PREFIX = "driver:alive:"
        private val ALIVE_TTL: Duration = Duration.ofSeconds(30)
        private const val MAX_RESULTS = 20L
        private const val MAX_LAT = 85.05112878
    }

    /** Lưu/cập nhật vị trí driver và gia hạn heartbeat. */
    fun saveDriverLocation(driverId: Long, location: GeoPoint) {
        validate(location)
        redisTemplate.opsForGeo().add(GEO_KEY, Point(location.lng, location.lat), driverId.toString())
        redisTemplate.opsForValue().set(aliveKey(driverId), "1", ALIVE_TTL)
    }

    /** Xóa driver khỏi tập khả dụng (tắt app, nhận chuyến, đang trong chuyến...). */
    fun removeDriver(driverId: Long) {
        redisTemplate.opsForGeo().remove(GEO_KEY, driverId.toString())
        redisTemplate.delete(aliveKey(driverId))
    }

    /** Tìm driver còn online trong bán kính [radiusKm], gần nhất trước. */
    fun findNearbyDrivers(center: GeoPoint, radiusKm: Double): List<NearbyDriver> {
        validate(center)
        require(radiusKm > 0) { "radiusKm phải lớn hơn 0" }

        val searchArea = Circle(Point(center.lng, center.lat), Distance(radiusKm, Metrics.KILOMETERS))
        val args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
            .includeDistance()     // Trả thêm khoảng cách
            .includeCoordinates()  // Trả thêm tọa độ
            .sortAscending()       // Gần nhất trước
            .limit(MAX_RESULTS)

        val found = redisTemplate.opsForGeo()
            .radius(GEO_KEY, searchArea, args)
            ?.content
            ?.mapNotNull { result ->
                // Member trong Redis là String, bỏ qua phần tử không phải số
                result.content.name.toLongOrNull()?.let { id -> id to result }
            }
            ?: return emptyList()
        if (found.isEmpty()) return emptyList()

        // Lọc driver đã mất kết nối (heartbeat hết hạn)
        val alive = redisTemplate.opsForValue()
            .multiGet(found.map { (id, _) -> aliveKey(id) })
            ?: return emptyList()

        return found
            .filterIndexed { i, _ -> alive.getOrNull(i) != null }
            .map { (id, result) ->
                NearbyDriver(
                    driverId = id,
                    distanceKm = result.distance.value,
                    location = GeoPoint(lat = result.content.point.y, lng = result.content.point.x)
                )
            }
    }

    /** Dọn driver hết heartbeat khỏi GEO set. Cần @EnableScheduling. */
    @Scheduled(fixedDelay = 60_000)
    fun cleanupStaleDrivers() {
        val ids = redisTemplate.opsForZSet().range(GEO_KEY, 0, -1)?.toList() ?: return
        if (ids.isEmpty()) return

        val alive = redisTemplate.opsForValue()
            .multiGet(ids.map { ALIVE_PREFIX + it })
            ?: return
        val stale = ids.filterIndexed { i, _ -> alive.getOrNull(i) == null }
        if (stale.isNotEmpty()) {
            redisTemplate.opsForGeo().remove(GEO_KEY, *stale.toTypedArray())
        }
    }

    private fun aliveKey(driverId: Long) = "$ALIVE_PREFIX$driverId"

    private fun validate(p: GeoPoint) {
        require(p.lng in -180.0..180.0) { "lng phải trong khoảng -180..180" }
        require(p.lat in -MAX_LAT..MAX_LAT) { "lat phải trong khoảng -85.05..85.05" }
    }
}