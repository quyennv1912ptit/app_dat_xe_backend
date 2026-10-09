package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.booking.api.dto.request.CreateBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.EstimateBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.CancelBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.CompleteBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.DriverArrivedRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.DriverStartArrivingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.StartTripRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.ArriveDestinationRequest
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingStatusResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.CreateBookingResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.EstimateBookingResponse
import com.nhom15.app_dat_xe.booking.entity.Booking
import com.nhom15.app_dat_xe.booking.entity.BookingActor
import com.nhom15.app_dat_xe.booking.entity.BookingTransitionCommand
import com.nhom15.app_dat_xe.booking.entity.BookingTransitionEvent
import com.nhom15.app_dat_xe.booking.entity.BookingType
import com.nhom15.app_dat_xe.booking.entity.customerBookingActor
import com.nhom15.app_dat_xe.booking.entity.driverBookingActor
import com.nhom15.app_dat_xe.booking.repository.BookingRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.domain.GeoPoint
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.event.BookingCreated
import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.port.CustomerVehicleQueryPort
import com.nhom15.app_dat_xe.common.port.CustomerVehicleSnapshot
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.util.CodeGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val vehicleQueryPort: CustomerVehicleQueryPort,
    private val quoteProvider: BookingQuoteProvider,
    private val stateMachine: BookingStateMachine,
    private val eventPublisher: EventPublisher,
) {

    fun estimateBooking(actor: AuthUser, request: EstimateBookingRequest): EstimateBookingResponse {
        if (actor.role != Role.CUSTOMER) {
            throw ForbiddenException(message = "Chỉ khách hàng mới có thể báo giá chuyến đi")
        }
        validateEstimate(request)

        val now = Instant.now()
        val scheduledAt = request.scheduledAt
        if (scheduledAt != null && !scheduledAt.isAfter(now)) {
            throw BadRequestException(message = "scheduledAt phải nằm trong tương lai")
        }
        val input = BookingQuoteInput(
            pickup = GeoPoint(request.pickupLat, request.pickupLng),
            dropoff = GeoPoint(request.dropoffLat, request.dropoffLng),
            pricingAt = scheduledAt ?: now,
        )
        val quote = quoteProvider.quote(input)
        return EstimateBookingResponse(
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
    }

    @Transactional
    fun createBooking(actor: AuthUser, request: CreateBookingRequest): CreateBookingResponse {
        requireCustomer(actor)
        validateSchedule(request)

        val vehicle = findOwnedVehicle(request.vehicleId, actor.userId)
        val quoteInput = BookingQuoteInput(
            pickup = GeoPoint(request.pickupLat, request.pickupLng),
            dropoff = GeoPoint(request.dropoffLat, request.dropoffLng),
            pricingAt = request.scheduledAt ?: Instant.now(),
        )
        val quote = quoteProvider.quote(quoteInput)

        val pendingBooking = bookingRepository.saveAndFlush(
            request.toBooking(
                code = generateUniqueCode(),
                customerId = actor.userId,
                vehicleId = vehicle.id,
                quote = quote,
            ),
        )
        stateMachine.recordCreated(pendingBooking, actor.userId)

        val booking = if (request.type == BookingType.INSTANT) {
            startSearching(
                booking = pendingBooking,
                actorId = actor.userId,
                transmission = vehicle.transmission,
            )
        } else {
            pendingBooking
        }

        return booking.toCreateResponse()
    }

    @Transactional
    fun cancelBooking(actor: AuthUser, bookingId: Long, request: CancelBookingRequest): BookingStatusResponse {
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        if (request.reason.isBlank() || request.reason.length > 500) {
            throw BadRequestException(message = "Lý do hủy chuyến phải có nội dung và không vượt quá 500 ký tự")
        }
        val reason = request.reason.trim()
        val currentBooking = bookingRepository.findById(bookingId).orElseThrow {
            NotFoundException(ErrorCode.BOOKING_NOT_FOUND)
        }
        // CANCEL có nhiều trạng thái đầu vào; giữ giá trị trước khi state machine sửa entity.
        val previousStatus = currentBooking.status
        val booking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.CANCEL,
                actor = BookingActor(userId = actor.userId, role = actor.role),
                note = reason,
                cancelReason = reason,
            ),
        )
        return BookingStatusResponse(
            bookingId = booking.id,
            code = booking.code,
            previousStatus = previousStatus,
            currentStatus = booking.status,
            changedAt = booking.updatedAt,
        )
    }

    @Transactional
    fun startArriving(actor: AuthUser, bookingId: Long, request: DriverStartArrivingRequest): BookingStatusResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể bắt đầu đi đến điểm đón")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val note = request.note
        if (note != null && note.length > 1_000) {
            throw BadRequestException(message = "Ghi chú bắt đầu đi đón không được vượt quá 1000 ký tự")
        }

        // Dùng cùng state machine để giữ quyền, lịch sử và event trong giao dịch.
        val booking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.DRIVER_START_ARRIVING,
                actor = driverBookingActor(actor.userId),
                note = note,
            ),
        )
        return BookingStatusResponse(
            bookingId = booking.id,
            code = booking.code,
            previousStatus = BookingStatus.DRIVER_ASSIGNED,
            currentStatus = booking.status,
            changedAt = booking.updatedAt,
        )
    }

    @Transactional
    fun arrivePickup(actor: AuthUser, bookingId: Long, request: DriverArrivedRequest): BookingStatusResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể báo đến điểm đón")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val note = request.note
        if (note != null && note.length > 1_000) {
            throw BadRequestException(message = "Ghi chú đến điểm đón không được vượt quá 1000 ký tự")
        }

        // State machine kiểm tra tài xế và trạng thái, đồng thời lưu lịch sử và phát event.
        val booking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.DRIVER_ARRIVED,
                actor = driverBookingActor(actor.userId),
                note = note,
            ),
        )
        return BookingStatusResponse(
            bookingId = booking.id,
            code = booking.code,
            previousStatus = BookingStatus.DRIVER_ARRIVING,
            currentStatus = booking.status,
            changedAt = booking.updatedAt,
        )
    }

    @Transactional
    fun startTrip(actor: AuthUser, bookingId: Long, request: StartTripRequest): BookingStatusResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể bắt đầu chuyến")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val note = request.note
        if (note != null && note.length > 1_000) {
            throw BadRequestException(message = "Ghi chú bắt đầu chuyến không được vượt quá 1000 ký tự")
        }

        // State machine kiểm tra tài xế, trạng thái và xác nhận PICKUP trước khi đổi trạng thái.
        val booking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.START_TRIP,
                actor = driverBookingActor(actor.userId),
                note = note,
            ),
        )
        return BookingStatusResponse(
            bookingId = booking.id,
            code = booking.code,
            previousStatus = BookingStatus.VEHICLE_HANDOVER,
            currentStatus = booking.status,
            changedAt = booking.updatedAt,
        )
    }

    @Transactional
    fun arriveDestination(actor: AuthUser, bookingId: Long, request: ArriveDestinationRequest): BookingStatusResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể báo đến điểm trả")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val note = request.note
        if (note != null && note.length > 1_000) {
            throw BadRequestException(message = "Ghi chú đến điểm trả không được vượt quá 1000 ký tự")
        }

        // State machine kiểm tra tài xế và trạng thái, đồng thời lưu lịch sử và phát event.
        val booking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.ARRIVE_DESTINATION,
                actor = driverBookingActor(actor.userId),
                note = note,
            ),
        )
        return BookingStatusResponse(
            bookingId = booking.id,
            code = booking.code,
            previousStatus = BookingStatus.IN_PROGRESS,
            currentStatus = booking.status,
            changedAt = booking.updatedAt,
        )
    }

    @Transactional
    fun completeBooking(actor: AuthUser, bookingId: Long, request: CompleteBookingRequest): BookingStatusResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể hoàn tất chuyến")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val note = request.note
        if (note != null && note.length > 1_000) {
            throw BadRequestException(message = "Ghi chú hoàn tất chuyến không được vượt quá 1000 ký tự")
        }

        // State machine kiểm tra DROPOFF, chốt cước và lưu lịch sử/event cùng giao dịch.
        val booking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.COMPLETE,
                actor = driverBookingActor(actor.userId),
                note = note,
            ),
        )
        return BookingStatusResponse(
            bookingId = booking.id,
            code = booking.code,
            previousStatus = BookingStatus.VEHICLE_RETURNED,
            currentStatus = booking.status,
            changedAt = booking.updatedAt,
        )
    }

    private fun validateEstimate(request: EstimateBookingRequest) {
        if (request.vehicleId <= 0) {
            throw BadRequestException(message = "vehicleId phải lớn hơn 0")
        }
        if (request.pickupAddress.isBlank() || request.pickupAddress.length > 500) {
            throw BadRequestException(message = "Địa chỉ đón phải có nội dung và không vượt quá 500 ký tự")
        }
        if (request.dropoffAddress.isBlank() || request.dropoffAddress.length > 500) {
            throw BadRequestException(message = "Địa chỉ đến phải có nội dung và không vượt quá 500 ký tự")
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

    private fun requireCustomer(actor: AuthUser) {
        if (actor.role != Role.CUSTOMER) {
            throw ForbiddenException(message = "Chỉ khách hàng mới có thể tạo booking")
        }
    }

    private fun findOwnedVehicle(vehicleId: Long, customerId: Long): CustomerVehicleSnapshot =
        vehicleQueryPort.findOwnedVehicle(vehicleId, customerId)
            ?: throw NotFoundException(
                ErrorCode.VEHICLE_NOT_FOUND,
                "Không tìm thấy xe thuộc khách hàng hiện tại",
            )

    private fun CreateBookingRequest.toBooking(
        code: String,
        customerId: Long,
        vehicleId: Long,
        quote: BookingQuote,
    ) = Booking(
        code = code,
        customerId = customerId,
        vehicleId = vehicleId,
        type = type,
        scheduledAt = scheduledAt,
        pickupAddress = pickupAddress.trim(),
        pickup = GeoPoint(pickupLat, pickupLng),
        dropoffAddress = dropoffAddress.trim(),
        dropoff = GeoPoint(dropoffLat, dropoffLng),
        distanceKm = quote.distanceKm,
        estimatedFare = quote.estimatedFare,
    )

    private fun startSearching(
        booking: Booking,
        actorId: Long,
        transmission: Transmission,
    ): Booking {
        val searchingBooking = stateMachine.transition(
            BookingTransitionCommand(
                bookingId = booking.id,
                event = BookingTransitionEvent.START_SEARCH,
                actor = customerBookingActor(actorId),
                note = "Bắt đầu tìm tài xế cho booking tức thời",
            ),
        )

        eventPublisher.publish(
            BookingCreated(
                bookingId = searchingBooking.id,
                customerId = searchingBooking.customerId,
                pickup = searchingBooking.pickup,
                transmission = transmission,
            ),
        )
        return searchingBooking
    }

    private fun validateSchedule(request: CreateBookingRequest) {
        when (request.type) {
            BookingType.INSTANT -> {
                if (request.scheduledAt != null) {
                    throw BadRequestException(message = "Booking tức thời không được có scheduledAt")
                }
            }

            BookingType.SCHEDULED -> {
                val scheduledAt = request.scheduledAt
                    ?: throw BadRequestException(message = "Booking đặt lịch phải có scheduledAt")
                if (!scheduledAt.isAfter(Instant.now())) {
                    throw BadRequestException(message = "scheduledAt phải nằm trong tương lai")
                }
            }
        }
    }

    private fun generateUniqueCode(): String {
        repeat(MAX_CODE_ATTEMPTS) {
            val code = CodeGenerator.randomBookingCode()
            if (bookingRepository.findByCode(code) == null) return code
        }
        throw ConflictException(message = "Không thể tạo mã booking duy nhất, vui lòng thử lại")
    }

    private fun Booking.toCreateResponse() = CreateBookingResponse(
        id = id,
        code = code,
        status = status,
        estimatedFare = estimatedFare,
        createdAt = createdAt,
    )

    private companion object {
        const val MAX_CODE_ATTEMPTS = 5
    }
}
