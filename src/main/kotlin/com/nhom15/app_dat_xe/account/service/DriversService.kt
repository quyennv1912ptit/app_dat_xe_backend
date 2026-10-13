package com.nhom15.app_dat_xe.account.service

import com.nhom15.app_dat_xe.account.entity.Driver
import com.nhom15.app_dat_xe.account.repository.DriverRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DriversService(
    private val driverRepository: DriverRepository
) {

    fun findByPhoneNumber(phongNumber: String): Optional<Driver> {
        return driverRepository.findByPhoneNumber(phongNumber)
    }
}