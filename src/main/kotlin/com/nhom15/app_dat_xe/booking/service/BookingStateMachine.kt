package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.booking.entity.Booking
import com.nhom15.app_dat_xe.booking.entity.BookingActor
import com.nhom15.app_dat_xe.booking.entity.BookingStatusHistory
import com.nhom15.app_dat_xe.booking.entity.BookingTransitionCommand
import com.nhom15.app_dat_xe.booking.entity.BookingTransitionEvent
import com.nhom15.app_dat_xe.booking.entity.HandoverPhase
import com.nhom15.app_dat_xe.booking.entity.VehicleHandoverReport
import com.nhom15.app_dat_xe.booking.repository.BookingRepository
import com.nhom15.app_dat_xe.booking.repository.BookingStatusHistoryRepository
import com.nhom15.app_dat_xe.booking.repository.VehicleHandoverReportRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.domain.Money
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.event.BookingStatusChanged
import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.event.TripCompleted
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.port.BookingAssignmentResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingStateMachine(
    private val bookingRepository: BookingRepository,
    private val historyRepository: BookingStatusHistoryRepository,
    private val eventPublisher: EventPublisher,
    private val reportRepository: VehicleHandoverReportRepository,
) {

    private val assignedDriverEvents = setOf(
        BookingTransitionEvent.DRIVER_START_ARRIVING,
        BookingTransitionEvent.DRIVER_ARRIVED,
        BookingTransitionEvent.PICKUP_HANDOVER_CREATED,
        BookingTransitionEvent.START_TRIP,
        BookingTransitionEvent.ARRIVE_DESTINATION,
        BookingTransitionEvent.DROPOFF_HANDOVER_CREATED,
        BookingTransitionEvent.COMPLETE,
    )

    private val systemEvents = setOf(
        BookingTransitionEvent.NO_DRIVER_FOUND,
        BookingTransitionEvent.EXPIRE,
    )

    @Transactional
    fun transition(command: BookingTransitionCommand): Booking {
        if (command.event == BookingTransitionEvent.ASSIGN_DRIVER) {
            throw BadRequestException(
                message = "ASSIGN_DRIVER phải được xử lý qua tryAssignDriver",
            )
        }

        val booking = findBooking(command.bookingId)
        authorize(booking, command.event, command.actor)

        val previousStatus = booking.status
        val nextStatus = BookingTransitionRules.resolve(previousStatus, command.event)

        validateHandover(booking.id, command.event)

        if (command.event == BookingTransitionEvent.CANCEL) {
            val reason = command.cancelReason?.trim()
            if (reason.isNullOrEmpty()) {
                throw BadRequestException(message = "Lý do hủy chuyến không được để trống")
            }
            booking.cancelReason = reason
            booking.cancelledBy = command.actor.userId
        }

        if (nextStatus == BookingStatus.COMPLETED && booking.finalFare == null) {
            booking.finalFare = booking.estimatedFare
        }

        booking.status = nextStatus
        bookingRepository.saveAndFlush(booking)
        recordHistory(booking, previousStatus, nextStatus, command.actor.userId, command.note)
        publishStatusChanged(booking, previousStatus, nextStatus, command.actor.userId)

        if (nextStatus == BookingStatus.COMPLETED) {
            publishTripCompleted(booking)
        }

        return booking
    }

    @Transactional
    fun recordCreated(booking: Booking, actorId: Long) {
        recordHistory(
            booking = booking,
            from = null,
            to = BookingStatus.PENDING,
            actorId = actorId,
            note = "Khởi tạo booking",
        )
        publishStatusChanged(
            booking = booking,
            from = null,
            to = BookingStatus.PENDING,
            actorId = actorId,
        )
    }

    /**
     * Nhánh riêng cho Matching: câu UPDATE có điều kiện quyết định duy nhất ai thắng
     * khi nhiều tài xế nhận cùng một booking.
     */
    @Transactional
    fun tryAssignDriver(
        bookingId: Long,
        driverId: Long,
        note: String? = null,
    ): BookingAssignmentResult? {
        if (bookingId <= 0 || driverId <= 0) {
            throw BadRequestException(message = "bookingId và driverId phải lớn hơn 0")
        }

        val updatedRows = bookingRepository.assignDriverIfSearching(bookingId, driverId)
        if (updatedRows == 0) return null

        val booking = findBooking(bookingId)
        recordHistory(
            booking = booking,
            from = BookingStatus.SEARCHING,
            to = BookingStatus.DRIVER_ASSIGNED,
            actorId = driverId,
            note = note,
        )
        publishStatusChanged(
            booking = booking,
            from = BookingStatus.SEARCHING,
            to = BookingStatus.DRIVER_ASSIGNED,
            actorId = driverId,
        )
        return BookingAssignmentResult(
            bookingId = booking.id,
            customerId = booking.customerId,
            driverId = driverId,
        )
    }

    // Dữ liệu và ảnh cần được kiểm tra tại service tạo/sửa biên bản trước khi lưu.
    private fun validateHandover(bookingId: Long, event: BookingTransitionEvent) {
        when (event) {
            BookingTransitionEvent.PICKUP_HANDOVER_CREATED -> {
                requireReport(bookingId, HandoverPhase.PICKUP)
            }

            BookingTransitionEvent.START_TRIP -> {
                val pickup = requireReport(bookingId, HandoverPhase.PICKUP)
                if (!pickup.confirmedByCustomer) {
                    throw BadRequestException(
                        ErrorCode.HANDOVER_REPORT_REQUIRED,
                        "Khách hàng chưa xác nhận biên bản nhận xe",
                    )
                }
            }

            BookingTransitionEvent.DROPOFF_HANDOVER_CREATED,
            BookingTransitionEvent.COMPLETE -> {
                requireReport(bookingId, HandoverPhase.DROPOFF)
            }

            else -> Unit
        }
    }

    private fun requireReport(bookingId: Long, phase: HandoverPhase): VehicleHandoverReport {
        val report = reportRepository.findByBookingIdAndPhase(bookingId, phase)
        if (report == null) {
            throw BadRequestException(
                ErrorCode.HANDOVER_REPORT_REQUIRED,
                "Chưa có biên bản bàn giao $phase của chuyến này",
            )
        }
        return report
    }

    private fun findBooking(bookingId: Long): Booking =
        bookingRepository.findById(bookingId).orElseThrow {
            NotFoundException(ErrorCode.BOOKING_NOT_FOUND)
        }

    private fun authorize(booking: Booking, event: BookingTransitionEvent, actor: BookingActor) {
        if (actor.role == Role.ADMIN) return

        val isOwnerCustomer =
            actor.role == Role.CUSTOMER && actor.userId == booking.customerId

        val isAssignedDriver =
            actor.role == Role.DRIVER &&
                actor.userId != null &&
                actor.userId == booking.driverId

        val allowed = when {
            event == BookingTransitionEvent.START_SEARCH ->
                actor.isSystem || isOwnerCustomer

            event == BookingTransitionEvent.CANCEL ->
                isOwnerCustomer || isAssignedDriver

            event in assignedDriverEvents ->
                isAssignedDriver

            event in systemEvents ->
                actor.isSystem

            event == BookingTransitionEvent.ASSIGN_DRIVER ->
                false

            else -> false
        }

        if (!allowed) {
            throw ForbiddenException(message = "Bạn không có quyền thay đổi trạng thái booking này")
        }
    }

    private fun recordHistory(
        booking: Booking,
        from: BookingStatus?,
        to: BookingStatus,
        actorId: Long?,
        note: String?,
    ) {
        historyRepository.save(
            BookingStatusHistory(
                booking = booking,
                fromStatus = from,
                toStatus = to,
                actorId = actorId,
                note = note,
            ),
        )
    }

    private fun publishStatusChanged(
        booking: Booking,
        from: BookingStatus?,
        to: BookingStatus,
        actorId: Long?,
    ) {
        eventPublisher.publish(
            BookingStatusChanged(
                bookingId = booking.id,
                customerId = booking.customerId,
                driverId = booking.driverId,
                from = from,
                to = to,
                actorId = actorId,
            ),
        )
    }

    private fun publishTripCompleted(booking: Booking) {
        val driverId = booking.driverId
            ?: throw ConflictException(ErrorCode.INVALID_BOOKING_STATE, "Booking chưa có tài xế")
        val finalFare = booking.finalFare
            ?: throw ConflictException(ErrorCode.INVALID_BOOKING_STATE, "Booking chưa có giá cuối cùng")

        eventPublisher.publish(
            TripCompleted(
                bookingId = booking.id,
                customerId = booking.customerId,
                driverId = driverId,
                finalFare = Money(finalFare),
            ),
        )
    }
}
