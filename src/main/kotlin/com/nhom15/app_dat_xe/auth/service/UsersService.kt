package com.nhom15.app_dat_xe.service

import com.nhom15.app_dat_xe.entity.Users
import com.nhom15.app_dat_xe.auth.repository.UsersRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UsersService(
    private val usersRepository: UsersRepository
) {

    fun findByPhoneNumber(phoneNumber: String): Optional<Users> {
        return usersRepository.findByPhoneNumber(phoneNumber)
    }
}