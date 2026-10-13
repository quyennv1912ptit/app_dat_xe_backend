package com.nhom15.app_dat_xe.ride.repository

import com.nhom15.app_dat_xe.common.enums.RideStatus
import com.nhom15.app_dat_xe.ride.entity.Ride
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RideRepository : JpaRepository<Ride, Long> {

    fun findByIdAndCustomerId(id: Long, customerId: Long): Ride?

    fun findByIdAndDriverId(id: Long, driverId: Long): Ride?

    fun findAllByCustomerId(customerId: Long, pageable: Pageable): Page<Ride>

    fun findAllByDriverId(driverId: Long, pageable: Pageable): Page<Ride>

    fun findAllByCustomerIdAndStatus(customerId: Long, status: RideStatus, pageable: Pageable): Page<Ride>

    fun findAllByDriverIdAndStatus(driverId: Long, status: RideStatus, pageable: Pageable): Page<Ride>

    fun findAllByStatus(status: RideStatus, pageable: Pageable): Page<Ride>

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
        update rides
        set driver_id = :driverId,
            accepted_at = CURRENT_TIMESTAMP,
            status = 'DRIVER_ASSIGNED',
            updated_at = CURRENT_TIMESTAMP,
            version = version + 1
        where id = :rideId
          and status = 'SEARCHING'
        """,
        nativeQuery = true,
    )
    fun assignDriverIfSearching(
        @Param("rideId") rideId: Long,
        @Param("driverId") driverId: Long,
    ): Int
}
