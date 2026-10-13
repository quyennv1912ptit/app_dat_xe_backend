package com.nhom15.app_dat_xe.account.service

import com.nhom15.app_dat_xe.account.entity.User
import com.nhom15.app_dat_xe.account.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UsersService(
    private val userRepository: UserRepository
) {

    fun findByPhoneNumber(phoneNumber: String): Optional<User> {
        return userRepository.findByPhoneNumber(phoneNumber)
    }
}