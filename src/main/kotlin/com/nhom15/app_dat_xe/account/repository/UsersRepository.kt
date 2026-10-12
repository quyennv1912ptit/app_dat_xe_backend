package com.nhom15.app_dat_xe.account.repository

import com.nhom15.app_dat_xe.account.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UsersRepository : JpaRepository<Users, Long> {

    fun findByPhoneNumber(phoneNumber: String): Optional<Users>

    fun findByEmail(email: String): Optional<Users>

    fun findByFirebaseUid(firebaseUid: String): Optional<Users>
}