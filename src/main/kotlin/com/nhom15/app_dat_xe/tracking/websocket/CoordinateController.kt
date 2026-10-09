package com.nhom15.app_dat_xe.tracking.websocket

import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class CoordinateController {

    @SendTo("/topic/driver-coordinates")
    fun broadcastCoordinates(@Payload currentCoordinates: String): String {
        println("SERVER RECEIVED COORDINATES: $currentCoordinates")
        return currentCoordinates
    }
}