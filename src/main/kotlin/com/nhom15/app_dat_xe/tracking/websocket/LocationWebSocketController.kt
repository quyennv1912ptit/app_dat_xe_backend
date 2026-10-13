package com.nhom15.app_dat_xe.tracking.websocket

import com.nhom15.app_dat_xe.tracking.dto.DriverLocation
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import com.nhom15.app_dat_xe.ride.repository.ACTIVE_RIDE_STATUSES
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
    private val locationService: LocationService,
    private val rideRepository: RideRepository,
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

        locationService.saveDriverLocation(
            currentCoordinate.driverId,
            currentCoordinate.location
        )

        val activeRide = rideRepository.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(
            currentCoordinate.driverId,
            ACTIVE_RIDE_STATUSES,
        ) ?: return

        messagingTemplate.convertAndSendToUser(
            Recipient.User(activeRide.customerId).key,
            "/queue/rides/${activeRide.id}/location",
            currentCoordinate,
        )
    }
}
