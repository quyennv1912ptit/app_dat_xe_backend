package com.nhom15.app_dat_xe.account.service

import com.nhom15.app_dat_xe.account.entity.Drivers
import com.nhom15.app_dat_xe.account.repository.DriversRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DriversService(
    private val driversRepository: DriversRepository
) {

    fun findByPhoneNumber(phongNumber: String): Optional<Drivers> {
        return driversRepository.findByPhoneNumber(phongNumber)
    }
}