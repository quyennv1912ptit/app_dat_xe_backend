package com.nhom15.app_dat_xe.tracking.controller

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver
import com.nhom15.app_dat_xe.tracking.dto.Route
import com.nhom15.app_dat_xe.tracking.service.MapService
import com.nhom15.app_dat_xe.tracking.service.LocationService
import org.springframework.http.ResponseEntity
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
    fun getCoordinates(@RequestParam address: String): ResponseEntity<String> {
        val result = mapService.getCoordinatesFromAddress(address)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/directions")
    fun getDirections(@RequestParam origin: String, @RequestParam destination: String): ResponseEntity<Route> {
        val result = mapService.getDistanceAndDuration(origin, destination)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/nearby-drivers")
    fun getNearbyDrivers(@RequestParam longitude: Double, @RequestParam latitude: Double, @RequestParam radius: Double): ResponseEntity<List<NearbyDriver>> {
        val result = locationService.findNearbyDrivers(GeoPoint(longitude, latitude), radius)
        return ResponseEntity.ok(result)
    }
}