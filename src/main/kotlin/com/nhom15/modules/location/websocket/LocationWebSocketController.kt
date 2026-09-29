package com.nhom15.app_dat_xe.modules.location.websocket

import com.nhom15.app_dat_xe.modules.location.dto.CoordinateDto
import com.nhom15.app_dat_xe.modules.location.dto.RouteDto
import com.nhom15.app_dat_xe.modules.location.service.MapService
import com.nhom15.app_dat_xe.modules.location.service.RedisGeoService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class LocationWebSocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val mapService: MapService,
    private val redisGeoService: RedisGeoService
) {

    @MessageMapping("/location")
    fun broadcastLocation(@Payload currentCoordinate: CoordinateDto) {
        println("SUCCESSFULLY RECEIVED COORDINATES: $currentCoordinate")
        messagingTemplate.convertAndSend("/topic/driver-location", currentCoordinate)

        redisGeoService.saveDriverLocation(
            currentCoordinate.driverId,
            currentCoordinate.longitude,
            currentCoordinate.latitude
        )

        val expectedLat = 20.980635
        val expectedLon = 105.787498

        val hasDeviated = mapService.checkRouteDeviation(
            currentCoordinate.latitude, currentCoordinate.longitude,
            expectedLat, expectedLon
        )

        if (hasDeviated) {
            val origin = "${currentCoordinate.longitude},${currentCoordinate.latitude}"
            val destination = "$expectedLon,$expectedLat"

            val newRoute = mapService.getDistanceAndDuration(origin, destination)
            messagingTemplate.convertAndSend("/topic/new-route", newRoute)
        }
    }
}