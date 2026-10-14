package com.nhom15.app_dat_xe.ride.service

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.api.PageResponse
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.ride.dto.request.RideListFilter
import com.nhom15.app_dat_xe.ride.dto.response.HandoverReportResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideDetailResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideStatusHistoryResponse
import com.nhom15.app_dat_xe.ride.dto.response.RideSummaryResponse
import com.nhom15.app_dat_xe.ride.entity.HandoverPhase
import com.nhom15.app_dat_xe.ride.repository.HandoverPhotoRepository
import com.nhom15.app_dat_xe.ride.repository.ACTIVE_RIDE_STATUSES
import com.nhom15.app_dat_xe.ride.repository.RideRepository
import com.nhom15.app_dat_xe.ride.repository.RideStatusHistoryRepository
import com.nhom15.app_dat_xe.ride.repository.VehicleHandoverReportRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RideQueryService(
    private val rideRepository: RideRepository,
    private val historyRepository: RideStatusHistoryRepository,
    private val reportRepository: VehicleHandoverReportRepository,
    private val photoRepository: HandoverPhotoRepository,
) {
    @Transactional(readOnly = true)
    fun getActiveRide(actor: AuthUser): RideDetailResponse? {
        val ride = when (actor.role) {
            Role.CUSTOMER -> rideRepository.findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(
                actor.id,
                ACTIVE_RIDE_STATUSES,
            )
            Role.DRIVER -> rideRepository.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(
                actor.id,
                ACTIVE_RIDE_STATUSES,
            )
            Role.ADMIN -> throw ForbiddenException(message = "Chỉ khách hàng và tài xế được xem chuyến đang hoạt động")
        } ?: return null

        return getDetail(actor, requireNotNull(ride.id))
    }

    // Đọc và tạo DTO trong transaction;
    @Transactional(readOnly = true)
    fun getDetail(actor: AuthUser, rideId: Long): RideDetailResponse {
        if (rideId <= 0) {
            throw BadRequestException(message = "rideId phải lớn hơn 0")
        }
        val ride = rideRepository.findById(rideId).orElseThrow {
            NotFoundException(ErrorCode.RIDE_NOT_FOUND)
        }
        val allowed = when (actor.role) {
            Role.CUSTOMER -> actor.id == ride.customerId
            Role.DRIVER -> ride.driverId != null && actor.id == ride.driverId
            Role.ADMIN -> true
        }
        if (!allowed) {
            throw ForbiddenException(message = "Bạn không có quyền xem chuyến này")
        }

        // Chỉ đọc dữ liệu con sau khi quyền trên ride đã được kiểm tra.
        val histories = historyRepository.findAllByRideIdOrderByCreatedAtAscIdAsc(rideId)
        val historyResponses = mutableListOf<RideStatusHistoryResponse>()
        for (history in histories) {
            historyResponses.add(RideResponseMapper.history(history))
        }

        val reportResponses = mutableListOf<HandoverReportResponse>()
        for (phase in listOf(HandoverPhase.PICKUP, HandoverPhase.DROPOFF)) {
            val report = reportRepository.findByRideIdAndPhase(rideId, phase)
            if (report != null) {
                val photos = photoRepository.findAllByReportIdOrderByCreatedAtAscIdAsc(report.id)
                reportResponses.add(HandoverResponseMapper.toResponse(report, photos))
            }
        }

        return RideResponseMapper.detail(ride, historyResponses, reportResponses)
    }

    @Transactional(readOnly = true)
    fun listMyRides(
        actor: AuthUser,
        filter: RideListFilter,
    ): PageResponse<RideSummaryResponse> {
        if (actor.role != Role.CUSTOMER && actor.role != Role.DRIVER) {
            throw ForbiddenException(message = "Chỉ khách hàng và tài xế được xem lịch sử cá nhân")
        }
        if (filter.page < 0) {
            throw BadRequestException(message = "page phải lớn hơn hoặc bằng 0")
        }
        if (filter.size < 1 || filter.size > 100) {
            throw BadRequestException(message = "size phải nằm trong khoảng 1 đến 100")
        }

        // id giúp thứ tự nhất quán khi nhiều ride có cùng thời gian tạo.
        val sort = Sort.by(Sort.Direction.DESC, "createdAt", "id")
        val pageable = PageRequest.of(filter.page, filter.size, sort)
        val statuses = parseStatuses(filter.status)

        // Luôn lấy id từ principal, không lấy customerId/driverId do client gửi.
        val rides = when (actor.role) {
            Role.CUSTOMER -> {
                if (statuses == null) {
                    rideRepository.findAllByCustomerId(actor.id, pageable)
                } else {
                    rideRepository.findAllByCustomerIdAndStatusIn(actor.id, statuses, pageable)
                }
            }
            Role.DRIVER -> {
                if (statuses == null) {
                    rideRepository.findAllByDriverId(actor.id, pageable)
                } else {
                    rideRepository.findAllByDriverIdAndStatusIn(actor.id, statuses, pageable)
                }
            }
            Role.ADMIN -> throw ForbiddenException()
        }

        return PageResponse.from(rides) { ride -> RideResponseMapper.summary(ride) }
    }

    @Transactional(readOnly = true)
    fun listAdminRides(
        actor: AuthUser,
        filter: RideListFilter,
    ): PageResponse<RideSummaryResponse> {
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
        val statuses = parseStatuses(filter.status)
        // Admin đọc toàn hệ thống; không lọc ride theo userId của admin.
        val rides = if (statuses == null) {
            rideRepository.findAll(pageable)
        } else {
            rideRepository.findAllByStatusIn(statuses, pageable)
        }

        return PageResponse.from(rides) { ride -> RideResponseMapper.summary(ride) }
    }

    private fun parseStatuses(raw: String?): List<RideStatus>? {
        if (raw == null) return null
        val values = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (values.isEmpty()) {
            throw BadRequestException(message = "status phải chứa ít nhất một trạng thái")
        }
        return values.distinct().map { value ->
            runCatching { RideStatus.valueOf(value.uppercase()) }.getOrElse {
                throw BadRequestException(message = "Trạng thái chuyến không hợp lệ: $value")
            }
        }
    }

}
