package com.nhom15.app_dat_xe.tracking.controller

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver
import com.nhom15.app_dat_xe.tracking.dto.Route
import com.nhom15.app_dat_xe.tracking.service.MapService
import com.nhom15.app_dat_xe.tracking.service.LocationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/location")
class MapController(
    private val mapService: MapService,
    private val locationService: LocationService
) {

    @GetMapping("/geocode")
    fun getCoordinates(
        @RequestParam address: String,
        @RequestParam(required = false) proximityLng: Double?,
        @RequestParam(required = false) proximityLat: Double?,
    ): ResponseEntity<String> {
        if ((proximityLng == null) != (proximityLat == null)) {
            throw BadRequestException(message = "proximityLng và proximityLat phải được truyền cùng nhau")
        }
        if (proximityLng != null && (!proximityLng.isFinite() || proximityLng !in -180.0..180.0)) {
            throw BadRequestException(message = "proximityLng phải nằm trong khoảng -180 đến 180")
        }
        if (proximityLat != null && (!proximityLat.isFinite() || proximityLat !in -90.0..90.0)) {
            throw BadRequestException(message = "proximityLat phải nằm trong khoảng -90 đến 90")
        }
        val proximity = if (proximityLng != null && proximityLat != null) {
            GeoPoint(lat = proximityLat, lng = proximityLng)
        } else null
        val result = mapService.getCoordinatesFromAddress(address, proximity)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/directions")
    fun getDirections(@RequestParam origin: String, @RequestParam destination: String): ResponseEntity<Route> {
        val result = mapService.getDistanceAndDuration(origin, destination)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/nearby-drivers")
    @PreAuthorize("hasRole('ADMIN')")
    fun getNearbyDrivers(@RequestParam longitude: Double, @RequestParam latitude: Double, @RequestParam radius: Double): ResponseEntity<List<NearbyDriver>> {
        val result = locationService.findNearbyDrivers(GeoPoint(lat = latitude, lng = longitude), radius)
        return ResponseEntity.ok(result)
    }
}
