package com.nhom15.app_dat_xe.account.repository

import com.nhom15.app_dat_xe.account.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByPhoneNumber(phoneNumber: String): Optional<User>

    fun findByEmail(email: String): Optional<User>

    fun findByFirebaseUid(firebaseUid: String): Optional<User>
}