package com.nhom15.app_dat_xe.tracking.websocket

import com.nhom15.app_dat_xe.tracking.dto.DriverLocation
import com.nhom15.app_dat_xe.tracking.service.MapService
import com.nhom15.app_dat_xe.tracking.service.LocationService
import com.nhom15.app_dat_xe.notification.api.Recipient
import com.nhom15.app_dat_xe.notification.ws.StompPrincipal
import org.springframework.messaging.MessagingException
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class LocationWebSocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val mapService: MapService,
    private val locationService: LocationService
) {

    @MessageMapping("/location")
    fun broadcastLocation(
        @Payload currentCoordinate: DriverLocation,
        principal: Principal,
    ) {
        val authenticatedDriver = (principal as? StompPrincipal)?.recipient as? Recipient.Driver
        if (authenticatedDriver?.id != currentCoordinate.driverId) {
            throw MessagingException("Tài khoản chỉ được gửi vị trí của chính mình")
        }

        messagingTemplate.convertAndSend("/topic/driver-location", currentCoordinate)

        locationService.saveDriverLocation(
            currentCoordinate.driverId,
            currentCoordinate.location
        )

        val expectedLat = 20.980635
        val expectedLon = 105.787498

        val hasDeviated = mapService.checkRouteDeviation(
            currentCoordinate.location.lat, currentCoordinate.location.lng,
            expectedLat, expectedLon
        )

        if (hasDeviated) {
            val origin = "${currentCoordinate.location.lng},${currentCoordinate.location.lat}"
            val destination = "$expectedLon,$expectedLat"

            val newRoute = mapService.getDistanceAndDuration(origin, destination)
            messagingTemplate.convertAndSend("/topic/new-route", newRoute)
        }
    }
}
