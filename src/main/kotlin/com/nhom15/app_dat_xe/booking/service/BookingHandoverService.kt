package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.booking.api.dto.request.CreateHandoverReportRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.ConfirmHandoverRequest
import com.nhom15.app_dat_xe.booking.api.dto.response.HandoverPhotoResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.HandoverReportResponse
import com.nhom15.app_dat_xe.booking.entity.BookingTransitionCommand
import com.nhom15.app_dat_xe.booking.entity.BookingTransitionEvent
import com.nhom15.app_dat_xe.booking.entity.HandoverPhase
import com.nhom15.app_dat_xe.booking.entity.HandoverPhoto
import com.nhom15.app_dat_xe.booking.entity.HandoverPhotoAngle
import com.nhom15.app_dat_xe.booking.entity.VehicleHandoverReport
import com.nhom15.app_dat_xe.booking.entity.driverBookingActor
import com.nhom15.app_dat_xe.booking.repository.BookingRepository
import com.nhom15.app_dat_xe.booking.repository.HandoverPhotoRepository
import com.nhom15.app_dat_xe.booking.repository.VehicleHandoverReportRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingHandoverService(
    private val bookingRepository: BookingRepository,
    private val reportRepository: VehicleHandoverReportRepository,
    private val photoRepository: HandoverPhotoRepository,
    private val stateMachine: BookingStateMachine,
) {
    // Biên bản, ảnh và trạng thái booking phải cùng thành công hoặc cùng rollback.
    @Transactional
    fun createPickup(
        actor: AuthUser,
        bookingId: Long,
        request: CreateHandoverReportRequest,
    ): HandoverReportResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể tạo biên bản nhận xe")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }

        val booking = bookingRepository.findById(bookingId).orElseThrow {
            NotFoundException(ErrorCode.BOOKING_NOT_FOUND)
        }
        if (booking.driverId != actor.userId) {
            throw ForbiddenException(message = "Bạn không phải tài xế được gán cho chuyến này")
        }
        if (reportRepository.existsByBookingIdAndPhase(bookingId, HandoverPhase.PICKUP)) {
            throw ConflictException(message = "Chuyến này đã có biên bản nhận xe")
        }
        if (booking.status != BookingStatus.DRIVER_ARRIVED) {
            throw ConflictException(
                ErrorCode.INVALID_BOOKING_STATE,
                "Chỉ tạo biên bản nhận xe khi tài xế đã đến điểm đón",
            )
        }
        validateRequest(request, HandoverPhase.PICKUP)

        val report = VehicleHandoverReport(
            booking = booking,
            phase = HandoverPhase.PICKUP,
            odometer = request.odometer,
            fuelLevel = request.fuelLevel,
            notes = request.notes,
            confirmedByCustomer = false,
        )
        val savedReport = try {
            reportRepository.saveAndFlush(report)
        } catch (_: DataIntegrityViolationException) {
            // Unique booking/phase vẫn bảo vệ nếu hai request cùng vượt qua exists.
            throw ConflictException(message = "Dữ liệu bàn giao bị xung đột, vui lòng tải lại chuyến")
        }

        val photos = mutableListOf<HandoverPhoto>()
        for (photo in request.photos) {
            photos.add(HandoverPhoto(savedReport, photo.angle, photo.fileUrl))
        }
        val savedPhotos = photoRepository.saveAllAndFlush(photos)

        stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.PICKUP_HANDOVER_CREATED,
                actor = driverBookingActor(actor.userId),
            ),
        )
        return toResponse(savedReport, savedPhotos)
    }

    // Lưu trả xe và chuyển trạng thái cùng transaction, giữ nguyên biên bản nhận xe.
    @Transactional
    fun createDropoff(
        actor: AuthUser,
        bookingId: Long,
        request: CreateHandoverReportRequest,
    ): HandoverReportResponse {
        if (actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ tài xế được gán mới có thể tạo biên bản trả xe")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }

        val booking = bookingRepository.findById(bookingId).orElseThrow {
            NotFoundException(ErrorCode.BOOKING_NOT_FOUND)
        }
        if (booking.driverId != actor.userId) {
            throw ForbiddenException(message = "Bạn không phải tài xế được gán cho chuyến này")
        }
        if (reportRepository.existsByBookingIdAndPhase(bookingId, HandoverPhase.DROPOFF)) {
            throw ConflictException(message = "Chuyến này đã có biên bản trả xe")
        }
        if (booking.status != BookingStatus.ARRIVED_DESTINATION) {
            throw ConflictException(
                ErrorCode.INVALID_BOOKING_STATE,
                "Chỉ tạo biên bản trả xe khi tài xế đã đến điểm trả",
            )
        }
        validateRequest(request, HandoverPhase.DROPOFF)

        val pickup = reportRepository.findByBookingIdAndPhase(bookingId, HandoverPhase.PICKUP)
            ?: throw BadRequestException(
                ErrorCode.HANDOVER_REPORT_REQUIRED,
                "Chưa có biên bản nhận xe để đối chiếu số km",
            )
        if (request.odometer < pickup.odometer) {
            throw BadRequestException(message = "Số km trả xe không được nhỏ hơn số km nhận xe")
        }

        val report = VehicleHandoverReport(
            booking = booking,
            phase = HandoverPhase.DROPOFF,
            odometer = request.odometer,
            fuelLevel = request.fuelLevel,
            notes = request.notes,
            confirmedByCustomer = false,
        )
        val savedReport = try {
            reportRepository.saveAndFlush(report)
        } catch (_: DataIntegrityViolationException) {
            // Unique booking/phase vẫn bảo vệ nếu hai request cùng vượt qua exists.
            throw ConflictException(message = "Dữ liệu bàn giao bị xung đột, vui lòng tải lại chuyến")
        }

        val photos = mutableListOf<HandoverPhoto>()
        for (photo in request.photos) {
            photos.add(HandoverPhoto(savedReport, photo.angle, photo.fileUrl))
        }
        val savedPhotos = photoRepository.saveAllAndFlush(photos)

        stateMachine.transition(
            BookingTransitionCommand(
                bookingId = bookingId,
                event = BookingTransitionEvent.DROPOFF_HANDOVER_CREATED,
                actor = driverBookingActor(actor.userId),
            ),
        )
        return toResponse(savedReport, savedPhotos)
    }

    @Transactional
    fun confirmPickup(
        actor: AuthUser,
        bookingId: Long,
        request: ConfirmHandoverRequest,
    ): HandoverReportResponse {
        if (actor.role != Role.CUSTOMER) {
            throw ForbiddenException(message = "Chỉ khách hàng sở hữu chuyến mới có thể xác nhận nhận xe")
        }
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val booking = bookingRepository.findById(bookingId).orElseThrow {
            NotFoundException(ErrorCode.BOOKING_NOT_FOUND)
        }
        if (booking.customerId != actor.userId) {
            throw ForbiddenException(message = "Bạn không phải khách hàng sở hữu chuyến này")
        }
        if (booking.status != BookingStatus.VEHICLE_HANDOVER) {
            throw ConflictException(
                ErrorCode.INVALID_BOOKING_STATE,
                "Chỉ xác nhận nhận xe khi booking đang ở trạng thái bàn giao xe",
            )
        }
        if (!request.confirmed) {
            throw BadRequestException(message = "Khách hàng phải xác nhận biên bản bàn giao")
        }
        val report = reportRepository.findByBookingIdAndPhase(bookingId, HandoverPhase.PICKUP)
            ?: throw BadRequestException(
                ErrorCode.HANDOVER_REPORT_REQUIRED,
                "Chưa có biên bản nhận xe của chuyến này",
            )

        // Xác nhận lặp chỉ đọc lại, tránh lưu và đổi updatedAt của biên bản.
        if (!report.confirmedByCustomer) {
            report.confirmedByCustomer = true
            reportRepository.saveAndFlush(report)
        }
        val photos = photoRepository.findAllByReportIdOrderByCreatedAtAsc(report.id)
        return toResponse(report, photos)
    }

    // Caller trực tiếp không đi qua @Valid của controller, nên service vẫn kiểm tra.
    private fun validateRequest(request: CreateHandoverReportRequest, phase: HandoverPhase) {
        val handoverName = if (phase == HandoverPhase.PICKUP) "nhận xe" else "trả xe"
        if (request.odometer < 0) {
            throw BadRequestException(message = "Số km $handoverName không được âm")
        }
        val fuelLevel = request.fuelLevel
        if (fuelLevel != null && (fuelLevel < 0 || fuelLevel > 100)) {
            throw BadRequestException(message = "Mức nhiên liệu phải nằm trong khoảng 0 đến 100")
        }
        val notes = request.notes
        if (notes != null && notes.length > 2_000) {
            throw BadRequestException(message = "Ghi chú bàn giao không được vượt quá 2000 ký tự")
        }
        if (request.photos.size < 4 || request.photos.size > 5) {
            throw BadRequestException(message = "Biên bản $handoverName phải có từ 4 đến 5 ảnh")
        }

        val angles = mutableSetOf<HandoverPhotoAngle>()
        for (photo in request.photos) {
            if (!angles.add(photo.angle)) {
                throw BadRequestException(message = "Góc ảnh bàn giao không được trùng")
            }
            if (photo.fileUrl.isBlank() || photo.fileUrl.length > 2_048) {
                throw BadRequestException(message = "Đường dẫn ảnh phải có nội dung và không quá 2048 ký tự")
            }
        }
        val requiredAngles = listOf(
            HandoverPhotoAngle.FRONT, HandoverPhotoAngle.BACK,
            HandoverPhotoAngle.LEFT, HandoverPhotoAngle.RIGHT,
        )
        for (angle in requiredAngles) {
            if (angle !in angles) {
                throw BadRequestException(message = "Biên bản $handoverName thiếu ảnh góc $angle")
            }
        }
    }

    private fun toResponse(report: VehicleHandoverReport, photos: List<HandoverPhoto>): HandoverReportResponse {
        val photoResponses = mutableListOf<HandoverPhotoResponse>()
        for (photo in photos) {
            photoResponses.add(HandoverPhotoResponse(photo.id, photo.angle, photo.fileUrl, photo.createdAt))
        }
        return HandoverReportResponse(
            id = report.id,
            phase = report.phase,
            odometer = report.odometer,
            fuelLevel = report.fuelLevel,
            notes = report.notes,
            confirmedByCustomer = report.confirmedByCustomer,
            photos = photoResponses,
            createdAt = report.createdAt,
        )
    }
}
