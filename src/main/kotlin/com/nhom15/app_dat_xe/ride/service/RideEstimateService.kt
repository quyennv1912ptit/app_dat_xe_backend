package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.ride.dto.internal.RideQuoteInput
import com.nhom15.app_dat_xe.ride.dto.request.EstimateRideRequest
import com.nhom15.app_dat_xe.ride.dto.response.EstimateRideResponse
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class RideEstimateService(
    private val quoteProvider: RideQuoteProvider,
) {
    fun estimateRide(actor: AuthUser, request: EstimateRideRequest): EstimateRideResponse {
        if (actor.role != Role.CUSTOMER) {
            throw ForbiddenException(message = "Chỉ khách hàng mới có thể báo giá chuyến đi")
        }
        validateEstimate(request)

        val now = Instant.now()
        val scheduledAt = request.scheduledAt
        if (scheduledAt != null && !scheduledAt.isAfter(now)) {
            throw BadRequestException(message = "scheduledAt phải nằm trong tương lai")
        }
        val input = RideQuoteInput(
            pickup = GeoPoint(request.pickupLat, request.pickupLng),
            dropoff = GeoPoint(request.dropoffLat, request.dropoffLng),
            pricingAt = scheduledAt ?: now,
        )
        val quote = quoteProvider.quote(input)
        return RideResponseMapper.estimate(quote)
    }

    private fun validateEstimate(request: EstimateRideRequest) {
        if (request.vehicleId <= 0) {
            throw BadRequestException(message = "vehicleId phải lớn hơn 0")
        }
        if (request.pickupAddress.isBlank() || request.pickupAddress.length > 255) {
            throw BadRequestException(message = "Địa chỉ đón phải có nội dung và không vượt quá 255 ký tự")
        }
        if (request.dropoffAddress.isBlank() || request.dropoffAddress.length > 255) {
            throw BadRequestException(message = "Địa chỉ đến phải có nội dung và không vượt quá 255 ký tự")
        }
        if (!request.pickupLat.isFinite() || request.pickupLat < -90.0 || request.pickupLat > 90.0) {
            throw BadRequestException(message = "pickupLat phải hữu hạn và từ -90 đến 90")
        }
        if (!request.pickupLng.isFinite() || request.pickupLng < -180.0 || request.pickupLng > 180.0) {
            throw BadRequestException(message = "pickupLng phải hữu hạn và từ -180 đến 180")
        }
        if (!request.dropoffLat.isFinite() || request.dropoffLat < -90.0 || request.dropoffLat > 90.0) {
            throw BadRequestException(message = "dropoffLat phải hữu hạn và từ -90 đến 90")
        }
        if (!request.dropoffLng.isFinite() || request.dropoffLng < -180.0 || request.dropoffLng > 180.0) {
            throw BadRequestException(message = "dropoffLng phải hữu hạn và từ -180 đến 180")
        }
    }
}
