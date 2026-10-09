package com.nhom15.app_dat_xe.ride.repository

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.ride.entity.Rides
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Chuyến đang diễn ra: từ lúc tìm tài xế đến khi trả xe, chưa COMPLETED.
 * Không gồm PENDING (chuyến đặt lịch chưa đến giờ không được chặn khách đặt chuyến khác)
 * và không gồm các trạng thái kết thúc.
 */
val ACTIVE_RIDE_STATUSES: List<RideStatus> = listOf(
    RideStatus.SEARCHING,
    RideStatus.DRIVER_ASSIGNED,
    RideStatus.DRIVER_ARRIVING,
    RideStatus.DRIVER_ARRIVED,
    RideStatus.VEHICLE_HANDOVER,
    RideStatus.IN_PROGRESS,
    RideStatus.ARRIVED_DESTINATION,
    RideStatus.VEHICLE_RETURNED
)

@Repository
interface RidesRepository : JpaRepository<Rides, Long> {

    // ===== Chuyến đang diễn ra (dựng lại Redis, chặn đặt trùng) =====

    fun findFirstByCustomerIdAndStatusInOrderByCreatedAtDesc(
        customerId: Long,
        statuses: Collection<RideStatus>
    ): Rides?

    fun existsByCustomerIdAndStatusIn(
        customerId: Long,
        statuses: Collection<RideStatus>
    ): Boolean

    fun findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(
        driverId: Long,
        statuses: Collection<RideStatus>
    ): Rides?

    // ===== Job quét =====

    /** Chuyến kẹt ở một trạng thái quá lâu (ví dụ SEARCHING mà dispatch đã bị mất). */
    fun findByStatusAndUpdatedAtBefore(status: RideStatus, cutoff: Instant): List<Rides>

    /** Chuyến đặt ngay (không có lịch) kẹt ở PENDING: đã lưu DB nhưng event dispatch bị mất. */
    fun findByStatusAndScheduledAtIsNullAndUpdatedAtBefore(status: RideStatus, cutoff: Instant): List<Rides>

    /** Chuyến đặt lịch đã đến lúc xử lý (gần giờ đón để dispatch, hoặc quá giờ để chuyển EXPIRED). */
    fun findByStatusAndScheduledAtLessThanEqual(status: RideStatus, time: Instant): List<Rides>

    // ===== Lịch sử chuyến =====

    fun findByCustomerIdOrderByCreatedAtDesc(customerId: Long, pageable: Pageable): Page<Rides>

    fun findByDriverIdOrderByCreatedAtDesc(driverId: Long, pageable: Pageable): Page<Rides>
}