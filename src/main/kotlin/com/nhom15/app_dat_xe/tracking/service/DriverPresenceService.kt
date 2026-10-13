package com.nhom15.app_dat_xe.tracking.service

import com.nhom15.app_dat_xe.account.repository.DriverRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.DriverStatus
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.redis.RedisKeys
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DriverPresenceService(
    private val driverRepository: DriverRepository,
    private val locationService: LocationService,
    private val redisTemplate: StringRedisTemplate,
) {

    @Transactional
    fun setOnline(driverId: Long, online: Boolean): DriverStatus {
        val driver = driverRepository.findById(driverId).orElseThrow {
            NotFoundException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài xế")
        }

        if (online) {
            if (driver.licenseVerified != true || driver.isFlagged == true) {
                throw ForbiddenException(
                    ErrorCode.DRIVER_NOT_VERIFIED,
                    "Tài xế chưa đủ điều kiện nhận chuyến",
                )
            }
            val id = driverId.toString()
            val hasOffer = redisTemplate.hasKey(RedisKeys.driverLock(id)) == true
            val hasActiveRide = redisTemplate.hasKey(RedisKeys.driverCurrentRide(id)) == true
            if (driver.status == DriverStatus.BUSY || hasOffer || hasActiveRide) {
                throw ConflictException(ErrorCode.DRIVER_NOT_AVAILABLE)
            }

            driver.status = DriverStatus.ONLINE
            driverRepository.save(driver)
            locationService.markDriverOnline(driverId)
            return driver.status
        }

        val id = driverId.toString()
        val hasOffer = redisTemplate.hasKey(RedisKeys.driverLock(id)) == true
        val hasActiveRide = redisTemplate.hasKey(RedisKeys.driverCurrentRide(id)) == true
        if (hasOffer || hasActiveRide || driver.status == DriverStatus.BUSY) {
            throw ConflictException(ErrorCode.DRIVER_NOT_AVAILABLE)
        }

        driver.status = DriverStatus.OFFLINE
        driverRepository.save(driver)
        locationService.removeDriver(driverId)
        return driver.status
    }
}
