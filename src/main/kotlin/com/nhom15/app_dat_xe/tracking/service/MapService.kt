package com.nhom15.app_dat_xe.tracking.service

import com.nhom15.app_dat_xe.tracking.dto.Route
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class MapService {

    @Value("\${mapbox.api.navigation.key}")
    private lateinit var mapboxToken: String

    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    fun getCoordinatesFromAddress(address: String): String? {
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$address.json?access_token=${mapboxToken.trim()}"
        return restTemplate.getForObject(url, String::class.java)
    }

    fun getDistanceAndDuration(origin: String, destination: String): Route {
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving-traffic/$origin;$destination?access_token=${mapboxToken.trim()}&geometries=geojson"
        val result = restTemplate.getForObject(url, String::class.java)

        return try {
            val rootNode: JsonNode = objectMapper.readTree(result)
            val routeNode: JsonNode = rootNode.path("routes").get(0)

            val distance = routeNode.path("distance").asDouble()
            val duration = routeNode.path("duration").asDouble()

            Route(distance, duration, result ?: "")
        } catch (e: Exception) {
            Route(0.0, 0.0, result ?: "")
        }
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371e3
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun checkRouteDeviation(currentLat: Double, currentLon: Double, expectedLat: Double, expectedLon: Double): Boolean {
        val distance = calculateDistance(currentLat, currentLon, expectedLat, expectedLon)
        val allowedThreshold = 15.0
        return distance > allowedThreshold
    }
}