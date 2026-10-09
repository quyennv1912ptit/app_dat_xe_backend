package com.nhom15.app_dat_xe.booking.service

import com.nhom15.app_dat_xe.booking.api.dto.request.BookingListFilter
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingDetailResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingStatusHistoryResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingSummaryResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.GeoPointResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.HandoverPhotoResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.HandoverReportResponse
import com.nhom15.app_dat_xe.booking.entity.Booking
import com.nhom15.app_dat_xe.booking.entity.HandoverPhase
import com.nhom15.app_dat_xe.booking.entity.HandoverPhoto
import com.nhom15.app_dat_xe.booking.entity.VehicleHandoverReport
import com.nhom15.app_dat_xe.booking.repository.BookingRepository
import com.nhom15.app_dat_xe.booking.repository.BookingStatusHistoryRepository
import com.nhom15.app_dat_xe.booking.repository.HandoverPhotoRepository
import com.nhom15.app_dat_xe.booking.repository.VehicleHandoverReportRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.api.PageResponse
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingQueryService(
    private val bookingRepository: BookingRepository,
    private val historyRepository: BookingStatusHistoryRepository,
    private val reportRepository: VehicleHandoverReportRepository,
    private val photoRepository: HandoverPhotoRepository,
) {
    // Đọc và tạo DTO trong transaction;
    @Transactional(readOnly = true)
    fun getDetail(actor: AuthUser, bookingId: Long): BookingDetailResponse {
        if (bookingId <= 0) {
            throw BadRequestException(message = "bookingId phải lớn hơn 0")
        }
        val booking = bookingRepository.findById(bookingId).orElseThrow {
            NotFoundException(ErrorCode.BOOKING_NOT_FOUND)
        }
        val allowed = when (actor.role) {
            Role.CUSTOMER -> actor.userId == booking.customerId
            Role.DRIVER -> booking.driverId != null && actor.userId == booking.driverId
            Role.ADMIN -> true
        }
        if (!allowed) {
            throw ForbiddenException(message = "Bạn không có quyền xem chuyến này")
        }

        // Chỉ đọc dữ liệu con sau khi quyền trên booking đã được kiểm tra.
        val histories = historyRepository.findAllByBookingIdOrderByCreatedAtAscIdAsc(bookingId)
        val historyResponses = mutableListOf<BookingStatusHistoryResponse>()
        for (history in histories) {
            historyResponses.add(BookingStatusHistoryResponse(
                id = history.id,
                fromStatus = history.fromStatus,
                toStatus = history.toStatus,
                actorId = history.actorId,
                note = history.note,
                createdAt = history.createdAt,
            ))
        }

        val reportResponses = mutableListOf<HandoverReportResponse>()
        for (phase in listOf(HandoverPhase.PICKUP, HandoverPhase.DROPOFF)) {
            val report = reportRepository.findByBookingIdAndPhase(bookingId, phase)
            if (report != null) {
                val photos = photoRepository.findAllByReportIdOrderByCreatedAtAscIdAsc(report.id)
                reportResponses.add(toHandoverResponse(report, photos))
            }
        }

        return BookingDetailResponse(
            id = booking.id,
            code = booking.code,
            customerId = booking.customerId,
            driverId = booking.driverId,
            vehicleId = booking.vehicleId,
            type = booking.type,
            scheduledAt = booking.scheduledAt,
            pickupAddress = booking.pickupAddress,
            pickup = GeoPointResponse(booking.pickup.lat, booking.pickup.lng),
            dropoffAddress = booking.dropoffAddress,
            dropoff = GeoPointResponse(booking.dropoff.lat, booking.dropoff.lng),
            distanceKm = booking.distanceKm,
            estimatedFare = booking.estimatedFare,
            finalFare = booking.finalFare,
            status = booking.status,
            cancelReason = booking.cancelReason,
            cancelledBy = booking.cancelledBy,
            statusHistory = historyResponses,
            handoverReports = reportResponses,
            createdAt = booking.createdAt,
            updatedAt = booking.updatedAt,
        )
    }

    @Transactional(readOnly = true)
    fun listMyBookings(
        actor: AuthUser,
        filter: BookingListFilter,
    ): PageResponse<BookingSummaryResponse> {
        if (actor.role != Role.CUSTOMER && actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ khách hàng và tài xế được xem lịch sử cá nhân")
        }
        if (filter.page < 0) {
            throw BadRequestException(message = "page phải lớn hơn hoặc bằng 0")
        }
        if (filter.size < 1 || filter.size > 100) {
            throw BadRequestException(message = "size phải nằm trong khoảng 1 đến 100")
        }

        // id giúp thứ tự nhất quán khi nhiều booking có cùng thời gian tạo.
        val sort = Sort.by(Sort.Direction.DESC, "createdAt", "id")
        val pageable = PageRequest.of(filter.page, filter.size, sort)
        val status = filter.status

        // Luôn lấy id từ principal, không lấy customerId/driverId do client gửi.
        val bookings = when (actor.role) {
            Role.CUSTOMER -> {
                if (status == null) {
                    bookingRepository.findAllByCustomerId(actor.userId, pageable)
                } else {
                    bookingRepository.findAllByCustomerIdAndStatus(actor.userId, status, pageable)
                }
            }
            Role.DRIVER -> {
                if (status == null) {
                    bookingRepository.findAllByDriverId(actor.userId, pageable)
                } else {
                    bookingRepository.findAllByDriverIdAndStatus(actor.userId, status, pageable)
                }
            }
            Role.ADMIN -> throw ForbiddenException()
        }

        return PageResponse.from(bookings) { booking -> toSummary(booking) }
    }

    @Transactional(readOnly = true)
    fun listAdminBookings(
        actor: AuthUser,
        filter: BookingListFilter,
    ): PageResponse<BookingSummaryResponse> {
        if (actor.role != Role.ADMIN) {
            throw ForbiddenException(message = "Chỉ admin được xem danh sách toàn bộ chuyến")
        }
        if (filter.page < 0) {
            throw BadRequestException(message = "page phải lớn hơn hoặc bằng 0")
        }
        if (filter.size < 1 || filter.size > 100) {
            throw BadRequestException(message = "size phải nằm trong khoảng 1 đến 100")
        }

        val sort = Sort.by(Sort.Direction.DESC, "createdAt", "id")
        val pageable = PageRequest.of(filter.page, filter.size, sort)
        val status = filter.status
        // Admin đọc toàn hệ thống; không lọc booking theo userId của admin.
        val bookings = if (status == null) {
            bookingRepository.findAll(pageable)
        } else {
            bookingRepository.findAllByStatus(status, pageable)
        }

        return PageResponse.from(bookings) { booking -> toSummary(booking) }
    }

    private fun toHandoverResponse(report: VehicleHandoverReport, photos: List<HandoverPhoto>): HandoverReportResponse {
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

    private fun toSummary(booking: Booking) = BookingSummaryResponse(
        id = booking.id,
        code = booking.code,
        pickupAddress = booking.pickupAddress,
        dropoffAddress = booking.dropoffAddress,
        type = booking.type,
        status = booking.status,
        estimatedFare = booking.estimatedFare,
        finalFare = booking.finalFare,
        scheduledAt = booking.scheduledAt,
        createdAt = booking.createdAt,
    )
}
