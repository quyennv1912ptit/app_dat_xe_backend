package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.domain.Location
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.port.CustomerVehicleQueryPort
import com.nhom15.app_dat_xe.common.port.CustomerVehicleSnapshot
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.ride.dto.internal.RideQuote
import com.nhom15.app_dat_xe.ride.dto.internal.RideQuoteInput
import com.nhom15.app_dat_xe.ride.dto.request.CancelRideRequest
import com.nhom15.app_dat_xe.ride.dto.request.CreateRideRequest
import com.nhom15.app_dat_xe.ride.dto.response.CreateRideResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideStatusResponse
import com.nhom15.app_dat_xe.ride.entity.Ride
import com.nhom15.app_dat_xe.ride.entity.RideType
import com.nhom15.app_dat_xe.ride.event.RideRequested
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import com.nhom15.app_dat_xe.ride.repository.ACTIVE_RIDE_STATUSES
import com.nhom15.app_dat_xe.ride.service.state.RideActor
import com.nhom15.app_dat_xe.ride.service.state.RideStateMachine
import com.nhom15.app_dat_xe.ride.service.state.RideTransitionCommand
import com.nhom15.app_dat_xe.ride.service.state.RideTransitionEvent
import com.nhom15.app_dat_xe.ride.service.state.customerRideActor
import com.nhom15.app_dat_xe.voucher.service.VoucherService
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RideService(
    private val rideRepository: RideRepository,
    private val vehicleQueryPort: CustomerVehicleQueryPort,
    private val quoteProvider: RideQuoteProvider,
    private val stateMachine: RideStateMachine,
    private val eventPublisher: EventPublisher,
    private val voucherService: VoucherService,
) {

    @Transactional
    fun createRide(actor: AuthUser, request: CreateRideRequest): CreateRideResponse {
        requireCustomer(actor)
        validateCreateFields(request)
        validateSchedule(request)

        if (rideRepository.existsByCustomerIdAndStatusIn(actor.id, ACTIVE_RIDE_STATUSES)) {
            throw ConflictException(
                ErrorCode.RIDE_ALREADY_ACTIVE,
                "Bạn đang có một chuyến chưa hoàn tất",
            )
        }

        val vehicle = findOwnedVehicle(request.vehicleId, actor.id)
        val quoteInput = RideQuoteInput(
            pickup = GeoPoint(request.pickupLat, request.pickupLng),
            dropoff = GeoPoint(request.dropoffLat, request.dropoffLng),
            pricingAt = request.scheduledAt ?: Instant.now(),
        )
        val quote = quoteProvider.quote(quoteInput)
        val appliedVoucher = voucherService.calculate(actor, request.voucherCode, quote.estimatedFare)

        val pendingRide = rideRepository.saveAndFlush(
            request.toRide(
                customerId = actor.id,
                vehicle = vehicle,
                quote = quote,
                discountAmount = appliedVoucher?.discountAmount ?: 0,
            ),
        )
        appliedVoucher?.let { voucherService.consume(actor.id, it, pendingRide.id) }
        stateMachine.recordCreated(pendingRide, actor.id)

        val ride = if (request.type == RideType.INSTANT) {
            startSearching(
                ride = pendingRide,
                actorId = actor.id,
                transmission = vehicle.transmission,
            )
        } else {
            pendingRide
        }

        return RideResponseMapper.create(ride)
    }

    @Transactional
    fun cancelRide(actor: AuthUser, rideId: Long, request: CancelRideRequest): RideStatusResponse {
        if (rideId <= 0) {
            throw BadRequestException(message = "rideId phải lớn hơn 0")
        }
        if (request.reason.isBlank() || request.reason.length > 255) {
            throw BadRequestException(message = "Lý do hủy chuyến phải có nội dung và không vượt quá 255 ký tự")
        }
        val reason = request.reason.trim()
        val currentRide = rideRepository.findById(rideId).orElseThrow {
            NotFoundException(ErrorCode.RIDE_NOT_FOUND)
        }
        // CANCEL có nhiều trạng thái đầu vào; giữ giá trị trước khi state machine sửa entity.
        val previousStatus = currentRide.status
        val ride = stateMachine.transition(
            RideTransitionCommand(
                rideId = rideId,
                event = RideTransitionEvent.CANCEL,
                actor = RideActor(userId = actor.id, role = actor.role),
                note = reason,
                cancelReason = reason,
            ),
        )
        return RideResponseMapper.status(ride, previousStatus)
    }

    private fun requireCustomer(actor: AuthUser) {
        if (actor.role != Role.CUSTOMER) {
            throw ForbiddenException(message = "Chỉ khách hàng mới có thể tạo ride")
        }
    }

    private fun findOwnedVehicle(vehicleId: Long, customerId: Long): CustomerVehicleSnapshot =
        vehicleQueryPort.findOwnedVehicle(vehicleId, customerId)
            ?: throw NotFoundException(
                ErrorCode.VEHICLE_NOT_FOUND,
                "Không tìm thấy xe thuộc khách hàng hiện tại",
            )

    private fun CreateRideRequest.toRide(
        customerId: Long,
        vehicle: CustomerVehicleSnapshot,
        quote: RideQuote,
        discountAmount: Long,
    ) = Ride(
        customerId = customerId,
        vehicleId = vehicle.id,
        vehicleType = vehicle.vehicleType,
        transmission = vehicle.transmission,
        licensePlate = vehicle.licensePlate,
        paymentMethod = paymentMethod,
        note = note?.trim(),
        type = type.name,
        scheduledAt = scheduledAt,
        pickup = Location(GeoPoint(pickupLat, pickupLng), pickupAddress),
        dropoff = Location( GeoPoint(dropoffLat, dropoffLng), dropoffAddress),
        distanceKm = quote.distanceKm.toDouble(),
        fareEstimate = quote.estimatedFare,
    ).also { it.discountAmount = discountAmount }

    private fun startSearching(
        ride: Ride,
        actorId: Long,
        transmission: Transmission,
    ): Ride {
        val searchingRide = stateMachine.transition(
            RideTransitionCommand(
                rideId = ride.id,
                event = RideTransitionEvent.START_SEARCH,
                actor = customerRideActor(actorId),
                note = "Bắt đầu tìm tài xế cho ride tức thời",
            ),
        )

        eventPublisher.publish(
            RideRequested(
                rideId = searchingRide.id,
                customerId = searchingRide.customerId,
                pickup = searchingRide.pickup.geoPoint,
                vehicleType = searchingRide.vehicleType,
                scheduledAt = searchingRide.scheduledAt,
                transmission = transmission,
            ),
        )
        return searchingRide
    }

    private fun validateCreateFields(request: CreateRideRequest) {
        if (request.pickupAddress.isBlank() || request.pickupAddress.length > 255 ||
            request.dropoffAddress.isBlank() || request.dropoffAddress.length > 255
        ) {
            throw BadRequestException(message = "Địa chỉ phải có nội dung và không vượt quá 255 ký tự")
        }
        if (request.note != null && request.note.length > 500) {
            throw BadRequestException(message = "Ghi chú không được vượt quá 500 ký tự")
        }
    }

    private fun validateSchedule(request: CreateRideRequest) {
        when (request.type) {
            RideType.INSTANT -> {
                if (request.scheduledAt != null) {
                    throw BadRequestException(message = "Ride tức thời không được có scheduledAt")
                }
            }

            RideType.SCHEDULED -> {
                val scheduledAt = request.scheduledAt
                    ?: throw BadRequestException(message = "Ride đặt lịch phải có scheduledAt")
                if (!scheduledAt.isAfter(Instant.now())) {
                    throw BadRequestException(message = "scheduledAt phải nằm trong tương lai")
                }
            }
        }
    }
}
