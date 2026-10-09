package com.nhom15.app_dat_xe.booking.repository

import com.nhom15.app_dat_xe.booking.entity.Booking
import com.nhom15.app_dat_xe.common.enums.BookingStatus
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookingRepository : JpaRepository<Booking, Long> {

    fun findByCode(code: String): Booking?

    fun findByIdAndCustomerId(id: Long, customerId: Long): Booking?

    fun findByIdAndDriverId(id: Long, driverId: Long): Booking?

    fun findAllByCustomerId(customerId: Long, pageable: Pageable): Page<Booking>

    fun findAllByDriverId(driverId: Long, pageable: Pageable): Page<Booking>

    fun findAllByCustomerIdAndStatus(customerId: Long, status: BookingStatus, pageable: Pageable): Page<Booking>

    fun findAllByDriverIdAndStatus(driverId: Long, status: BookingStatus, pageable: Pageable): Page<Booking>

    fun findAllByStatus(status: BookingStatus, pageable: Pageable): Page<Booking>

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        update bookings
        set driver_id = :driverId,
            status = 'DRIVER_ASSIGNED',
            updated_at = CURRENT_TIMESTAMP,
            version = version + 1
        where id = :bookingId
          and status = 'SEARCHING'
        """,
        nativeQuery = true,
    )
    fun assignDriverIfSearching(
        @Param("bookingId") bookingId: Long,
        @Param("driverId") driverId: Long,
    ): Int
}
