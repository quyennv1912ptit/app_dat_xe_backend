package com.nhom15.app_dat_xe.account.repository

import com.nhom15.app_dat_xe.account.entity.Driver
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DriverRepository : JpaRepository<Driver, Long> {

    fun findByPhoneNumber(phoneNumber: String): Optional<Driver>

    fun findByFirebaseUid(firebaseUid: String): Optional<Driver>

    fun findByEmail(email: String): Optional<Driver>
}