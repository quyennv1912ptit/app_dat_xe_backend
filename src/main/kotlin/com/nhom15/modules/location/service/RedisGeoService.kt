package com.nhom15.app_dat_xe.modules.location.service

import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisGeoService(private val redisTemplate: StringRedisTemplate) {

    fun saveDriverLocation(driverId: String, longitude: Double, latitude: Double) {
        val location = Point(longitude, latitude)
        redisTemplate.opsForGeo().add("available_drivers", location, driverId)
    }

    fun findNearbyDrivers(longitude: Double, latitude: Double, radius: Double): List<String> {
        val center = Point(longitude, latitude)
        val distance = Distance(radius, Metrics.KILOMETERS)
        val searchArea = Circle(center, distance)

        val args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending()
        val results = redisTemplate.opsForGeo().radius("available_drivers", searchArea, args)

        val driverList = mutableListOf<String>()
        results?.forEach { result ->
            driverList.add(result.content.name)
        }
        return driverList
    }
}