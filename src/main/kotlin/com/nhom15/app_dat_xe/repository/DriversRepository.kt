package com.nhom15.app_dat_xe.repository

import com.nhom15.app_dat_xe.entity.Drivers
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DriversRepository : JpaRepository<Drivers, Long> {

    fun findByPhoneNumber(phoneNumber: String): Optional<Drivers>

    fun findByFirebaseUid(firebaseUid: String): Optional<Drivers>

    fun findByEmail(email: String): Optional<Drivers>
}