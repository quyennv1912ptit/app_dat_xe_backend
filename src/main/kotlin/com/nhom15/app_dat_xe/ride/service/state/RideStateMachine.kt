package com.nhom15.app_dat_xe.ride.service.state

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.domain.Money
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.event.EventPublisher
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.port.RideAssignmentResult
import com.nhom15.app_dat_xe.ride.entity.HandoverPhase
import com.nhom15.app_dat_xe.ride.entity.Ride
import com.nhom15.app_dat_xe.ride.entity.RideActorRole
import com.nhom15.app_dat_xe.ride.entity.RideStatusHistory
import com.nhom15.app_dat_xe.ride.entity.VehicleHandoverReport
import com.nhom15.app_dat_xe.ride.event.RideCompleted
import com.nhom15.app_dat_xe.ride.event.RideDriverAssigned
import com.nhom15.app_dat_xe.ride.event.RideStatusChanged
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import com.nhom15.app_dat_xe.ride.repository.RideStatusHistoryRepository
import com.nhom15.app_dat_xe.ride.repository.VehicleHandoverReportRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RideStateMachine(
    private val rideRepository: RideRepository,
    private val historyRepository: RideStatusHistoryRepository,
    private val eventPublisher: EventPublisher,
    private val reportRepository: VehicleHandoverReportRepository,
) {

    private val assignedDriverEvents = setOf(
        RideTransitionEvent.DRIVER_START_ARRIVING,
        RideTransitionEvent.DRIVER_ARRIVED,
        RideTransitionEvent.PICKUP_HANDOVER_CREATED,
        RideTransitionEvent.START_TRIP,
        RideTransitionEvent.ARRIVE_DESTINATION,
        RideTransitionEvent.DROPOFF_HANDOVER_CREATED,
        RideTransitionEvent.COMPLETE,
    )

    private val systemEvents = setOf(
        RideTransitionEvent.NO_DRIVER_FOUND,
        RideTransitionEvent.EXPIRE,
    )

    @Transactional
    fun transition(command: RideTransitionCommand): Ride {
        if (command.event == RideTransitionEvent.ASSIGN_DRIVER) {
            throw BadRequestException(
                message = "ASSIGN_DRIVER phải được xử lý qua tryAssignDriver",
            )
        }

        val ride = findRide(command.rideId)
        authorize(ride, command.event, command.actor)

        val previousStatus = ride.status
        val nextStatus = RideTransitionRules.resolve(previousStatus, command.event)

        validateHandover(ride.id, command.event)

        if (command.event == RideTransitionEvent.CANCEL) {
            val reason = command.cancelReason?.trim()
            if (reason.isNullOrEmpty() || reason.length > 255) {
                throw BadRequestException(message = "Lý do hủy chuyến phải có nội dung và không vượt quá 255 ký tự")
            }
            ride.cancelReason = reason
            ride.cancelledBy = command.actor.userId
            ride.cancelledByRole = command.actor.persistedRole
            ride.cancelledAt = Instant.now()
        }

        if (nextStatus == RideStatus.IN_PROGRESS) ride.startedAt = Instant.now()
        if (nextStatus == RideStatus.COMPLETED) ride.completedAt = Instant.now()

        if (nextStatus == RideStatus.COMPLETED && ride.finalFare == null) {
            ride.finalFare = ride.estimatedFare
        }

        ride.status = nextStatus
        rideRepository.saveAndFlush(ride)
        recordHistory(ride, previousStatus, nextStatus, command.actor.userId, command.note, command.actor.persistedRole)
        publishStatusChanged(ride, previousStatus, nextStatus, command.actor.userId)

        if (nextStatus == RideStatus.COMPLETED) {
            publishRideCompleted(ride)
        }

        return ride
    }

    @Transactional
    fun recordCreated(ride: Ride, actorId: Long) {
        recordHistory(
            ride = ride,
            from = null,
            to = RideStatus.PENDING,
            actorId = actorId,
            note = "Khởi tạo ride",
            actorRole = RideActorRole.USER,
        )
        publishStatusChanged(
            ride = ride,
            from = null,
            to = RideStatus.PENDING,
            actorId = actorId,
        )
    }

    /**
     * Nhánh riêng cho Matching: câu UPDATE có điều kiện quyết định duy nhất ai thắng
     * khi nhiều tài xế nhận cùng một ride.
     */
    @Transactional
    fun tryAssignDriver(
        rideId: Long,
        driverId: Long,
        note: String? = null,
    ): RideAssignmentResult? {
        if (rideId <= 0 || driverId <= 0) {
            throw BadRequestException(message = "rideId và driverId phải lớn hơn 0")
        }

        val updatedRows = rideRepository.assignDriverIfSearching(rideId, driverId)
        if (updatedRows == 0) return null

        val ride = findRide(rideId)
        recordHistory(
            ride = ride,
            from = RideStatus.SEARCHING,
            to = RideStatus.DRIVER_ASSIGNED,
            actorId = driverId,
            note = note,
            actorRole = RideActorRole.DRIVER,
        )
        publishStatusChanged(
            ride = ride,
            from = RideStatus.SEARCHING,
            to = RideStatus.DRIVER_ASSIGNED,
            actorId = driverId,
        )
        eventPublisher.publish(
            RideDriverAssigned(rideId = ride.id, customerId = ride.customerId, driverId = driverId),
        )
        return RideAssignmentResult(
            rideId = ride.id,
            customerId = ride.customerId,
            driverId = driverId,
        )
    }

    // Dữ liệu và ảnh cần được kiểm tra tại service tạo/sửa biên bản trước khi lưu.
    private fun validateHandover(rideId: Long, event: RideTransitionEvent) {
        when (event) {
            RideTransitionEvent.PICKUP_HANDOVER_CREATED -> {
                requireReport(rideId, HandoverPhase.PICKUP)
            }

            RideTransitionEvent.START_TRIP -> {
                val pickup = requireReport(rideId, HandoverPhase.PICKUP)
                if (!pickup.confirmedByCustomer) {
                    throw BadRequestException(
                        ErrorCode.HANDOVER_REPORT_REQUIRED,
                        "Khách hàng chưa xác nhận biên bản nhận xe",
                    )
                }
            }

            RideTransitionEvent.DROPOFF_HANDOVER_CREATED,
            RideTransitionEvent.COMPLETE -> {
                requireReport(rideId, HandoverPhase.DROPOFF)
            }

            else -> Unit
        }
    }

    private fun requireReport(rideId: Long, phase: HandoverPhase): VehicleHandoverReport {
        val report = reportRepository.findByRideIdAndPhase(rideId, phase)
        if (report == null) {
            throw BadRequestException(
                ErrorCode.HANDOVER_REPORT_REQUIRED,
                "Chưa có biên bản bàn giao $phase của chuyến này",
            )
        }
        return report
    }

    private fun findRide(rideId: Long): Ride =
        rideRepository.findById(rideId).orElseThrow {
            NotFoundException(ErrorCode.RIDE_NOT_FOUND)
        }

    private fun authorize(ride: Ride, event: RideTransitionEvent, actor: RideActor) {
        if (actor.role == Role.ADMIN) return

        val isOwnerCustomer =
            actor.role == Role.CUSTOMER && actor.userId == ride.customerId

        val isAssignedDriver =
            actor.role == Role.DRIVER &&
                actor.userId != null &&
                actor.userId == ride.driverId

        val allowed = when {
            event == RideTransitionEvent.START_SEARCH ->
                actor.isSystem || isOwnerCustomer

            event == RideTransitionEvent.CANCEL ->
                isOwnerCustomer || isAssignedDriver

            event in assignedDriverEvents ->
                isAssignedDriver

            event in systemEvents ->
                actor.isSystem

            event == RideTransitionEvent.ASSIGN_DRIVER ->
                false

            else -> false
        }

        if (!allowed) {
            throw ForbiddenException(message = "Bạn không có quyền thay đổi trạng thái ride này")
        }
    }

    private fun recordHistory(
        ride: Ride,
        from: RideStatus?,
        to: RideStatus,
        actorId: Long?,
        note: String?,
        actorRole: RideActorRole?,
    ) {
        historyRepository.save(
            RideStatusHistory(
                ride = ride,
                fromStatus = from,
                toStatus = to,
                actorId = actorId,
                note = note,
                actorRole = actorRole,
            ),
        )
    }

    private fun publishStatusChanged(
        ride: Ride,
        from: RideStatus?,
        to: RideStatus,
        actorId: Long?,
    ) {
        eventPublisher.publish(
            RideStatusChanged(
                rideId = ride.id,
                customerId = ride.customerId,
                driverId = ride.driverId,
                from = from,
                to = to,
                actorId = actorId,
            ),
        )
    }

    private fun publishRideCompleted(ride: Ride) {
        val driverId = ride.driverId
            ?: throw ConflictException(ErrorCode.INVALID_RIDE_STATE, "Ride chưa có tài xế")
        val finalFare = ride.finalFare
            ?: throw ConflictException(ErrorCode.INVALID_RIDE_STATE, "Ride chưa có giá cuối cùng")

        eventPublisher.publish(
            RideCompleted(
                rideId = ride.id,
                customerId = ride.customerId,
                driverId = driverId,
                finalFare = Money(finalFare),
            ),
        )
    }
}
