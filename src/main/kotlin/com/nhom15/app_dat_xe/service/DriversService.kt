package com.nhom15.app_dat_xe.service

import com.nhom15.app_dat_xe.entity.Drivers
import com.nhom15.app_dat_xe.repository.DriversRepository
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