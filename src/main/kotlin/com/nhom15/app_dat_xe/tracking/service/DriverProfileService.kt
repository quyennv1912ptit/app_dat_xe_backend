package com.nhom15.app_dat_xe.tracking.service

import com.nhom15.app_dat_xe.account.entity.Driver
import com.nhom15.app_dat_xe.account.repository.DriverRepository
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.tracking.dto.DriverProfileResponse
import com.nhom15.app_dat_xe.tracking.dto.UpdateDriverProfileRequest
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DriverProfileService(
    private val driverRepository: DriverRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Transactional(readOnly = true)
    fun get(driverId: Long): DriverProfileResponse = response(getDriver(driverId))

    @Transactional
    fun update(driverId: Long, request: UpdateDriverProfileRequest): DriverProfileResponse {
        val driver = getDriver(driverId)
        driver.licenseNumber = request.licenseNumber.trim()
        driver.licenseClass = request.licenseClass.trim().uppercase()
        driver.yearsOfExperience = request.yearsOfExperience
        val saved = driverRepository.save(driver)
        request.licenseImageUrl?.let { imageUrl ->
            val url = imageUrl.trim()
            if (!(url.startsWith("https://") || url.startsWith("http://"))) {
                throw BadRequestException(message = "Đường dẫn ảnh bằng lái không hợp lệ")
            }
            updateImageUrl(driverId, url)
        }
        return response(saved)
    }

    @Transactional
    fun review(driverId: Long, approved: Boolean): DriverProfileResponse {
        val driver = getDriver(driverId)
        if (approved && (driver.licenseNumber.isNullOrBlank() || driver.licenseClass.isNullOrBlank())) {
            throw BadRequestException(message = "Tài xế chưa khai báo đủ thông tin bằng lái")
        }
        driver.licenseVerified = approved
        return response(driverRepository.save(driver))
    }

    private fun response(driver: Driver): DriverProfileResponse {
        val id = requireNotNull(driver.id)
        val imageUrl = imageColumn()?.let { column ->
            jdbcTemplate.queryForObject("SELECT $column FROM drivers WHERE id = ?", { row, _ -> row.getString(1) }, id)
        }
        val complete = !driver.licenseNumber.isNullOrBlank() && !driver.licenseClass.isNullOrBlank()
        return DriverProfileResponse(
            id = id,
            licenseNumber = driver.licenseNumber,
            licenseClass = driver.licenseClass,
            yearsOfExperience = driver.yearsOfExperience,
            licenseImageUrl = imageUrl,
            licenseVerified = driver.licenseVerified,
            licenseReviewStatus = when {
                driver.licenseVerified -> "APPROVED"
                complete -> "PENDING"
                else -> "NOT_SUBMITTED"
            },
        )
    }

    private fun updateImageUrl(driverId: Long, url: String) {
        val column = imageColumn()
            ?: throw BadRequestException(message = "Cơ sở dữ liệu chưa có cột lưu ảnh bằng lái (license_image_url)")
        jdbcTemplate.update("UPDATE drivers SET $column = ? WHERE id = ?", url, driverId)
    }

    private fun imageColumn(): String? {
        val columns = jdbcTemplate.execute(ConnectionCallback { connection ->
            val result = mutableSetOf<String>()
            connection.metaData.getColumns(connection.catalog, null, "drivers", null).use { rows ->
                while (rows.next()) result += rows.getString("COLUMN_NAME").lowercase()
            }
            result
        }) ?: emptySet()
        return listOf("license_image_url", "license_photo_url", "license_url").firstOrNull(columns::contains)
    }

    private fun getDriver(driverId: Long): Driver {
        if (driverId <= 0) throw BadRequestException(message = "driverId phải lớn hơn 0")
        return driverRepository.findById(driverId).orElseThrow { NotFoundException(ErrorCode.USER_NOT_FOUND) }
    }
}
