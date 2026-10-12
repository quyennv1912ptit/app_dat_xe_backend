package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.ride.dto.internal.RideQuote
import com.nhom15.app_dat_xe.ride.dto.response.CreateRideResponse
import com.nhom15.app_dat_xe.ride.dto.response.EstimateRideResponse
import com.nhom15.app_dat_xe.ride.dto.response.GeoPointResponse
import com.nhom15.app_dat_xe.ride.dto.response.HandoverReportResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideDetailResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideStatusHistoryResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideStatusResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideSummaryResponse
import com.nhom15.app_dat_xe.ride.entity.Ride
import com.nhom15.app_dat_xe.ride.entity.RideStatusHistory

internal object RideResponseMapper {
    fun estimate(quote: RideQuote) = EstimateRideResponse(
        pricingRuleId = quote.pricingRuleId,
        distanceKm = quote.distanceKm,
        estimatedDurationMinutes = quote.estimatedDurationMinutes,
        baseFare = quote.baseFare,
        distanceFare = quote.distanceFare,
        waitingFare = quote.waitingFare,
        nightSurcharge = quote.nightSurcharge,
        returnFee = quote.returnFee,
        estimatedFare = quote.estimatedFare,
    )

    fun create(ride: Ride) = CreateRideResponse(
        id = ride.id, code = ride.code, status = ride.status,
        estimatedFare = ride.estimatedFare, createdAt = ride.createdAt,
    )

    fun status(ride: Ride, previousStatus: RideStatus) = RideStatusResponse(
        rideId = ride.id, code = ride.code, previousStatus = previousStatus,
        currentStatus = ride.status, changedAt = ride.updatedAt,
    )

    fun summary(ride: Ride) = RideSummaryResponse(
        id = ride.id, code = ride.code, pickupAddress = ride.pickupAddress,
        dropoffAddress = ride.dropoffAddress, type = ride.type, status = ride.status,
        estimatedFare = ride.estimatedFare, finalFare = ride.finalFare,
        scheduledAt = ride.scheduledAt, createdAt = ride.createdAt,
    )

    fun history(history: RideStatusHistory) = RideStatusHistoryResponse(
        id = history.id, fromStatus = history.fromStatus, toStatus = history.toStatus,
        actorId = history.actorId, note = history.note, createdAt = history.createdAt,
    )

    fun detail(
        ride: Ride,
        statusHistory: List<RideStatusHistoryResponse>,
        handoverReports: List<HandoverReportResponse>,
    ) = RideDetailResponse(
        id = ride.id, code = ride.code, customerId = ride.customerId,
        driverId = ride.driverId, vehicleId = ride.vehicleId, type = ride.type,
        scheduledAt = ride.scheduledAt, pickupAddress = ride.pickupAddress,
        pickup = GeoPointResponse(ride.pickupPoint.lat, ride.pickupPoint.lng),
        dropoffAddress = ride.dropoffAddress,
        dropoff = GeoPointResponse(ride.dropoffPoint.lat, ride.dropoffPoint.lng),
        distanceKm = ride.distanceKmDecimal, estimatedFare = ride.estimatedFare,
        finalFare = ride.finalFare, status = ride.status, cancelReason = ride.cancelReason,
        cancelledBy = ride.cancelledBy, statusHistory = statusHistory,
        handoverReports = handoverReports, createdAt = ride.createdAt, updatedAt = ride.updatedAt,
    )
}
