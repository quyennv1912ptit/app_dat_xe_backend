package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.account.entity.Driver
import org.springframework.stereotype.Service
import com.nhom15.app_dat_xe.account.repository.DriverRepository
import com.nhom15.app_dat_xe.common.enums.DriverStatus
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.matching.dto.RankedDriverDTO
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver

@Service
class DriverSelectionService(
    private val driverRepository: DriverRepository
) {
    fun filterAndRankDrivers(
        nearbyDrivers: List<NearbyDriver>,
        requiredTransmission: Transmission,
        radiusKm: Double,
    ): List<RankedDriverDTO> {
        if (nearbyDrivers.isEmpty()) return emptyList()
        require(radiusKm > 0.0) { "radiusKm phải lớn hơn 0" }

        // Tạo map id -> driver
        val driversById: Map<Long, Driver> = driverRepository
            .findAllById(nearbyDrivers.map { it.driverId })
            .associateBy { it.id }

        return nearbyDrivers
            .mapNotNull { nearby ->
                val driver = driversById[nearby.driverId] ?: return@mapNotNull null
                if (!isEligible(driver, requiredTransmission)) return@mapNotNull null

                RankedDriverDTO(
                    driverId = nearby.driverId,
                    distance = nearby.distanceKm,
                    score = calculateScore(driver, nearby.distanceKm, radiusKm)
                )
            }
            .sortedByDescending { it.score }
    }

    fun isEligible(driver: Driver, requiredTransmission: Transmission): Boolean {
        if (driver.status != DriverStatus.ONLINE) return false

        if (requiredTransmission == Transmission.MANUAL && driver.licenseClass == "B1") return false

        return true
    }

    private fun calculateScore(driver: Driver, distanceKm: Double, radiusKm: Double): Double {
        val rating = driver.averageRating
        val experience = driver.yearsOfExperience ?: 0

        var score = 0.0

        score += (rating ?: 5.0) * 10.0

        score += (experience * 2.0).coerceAtMost(10.0)

        // Giữ ảnh hưởng của rating khi tìm ở các vòng bán kính lớn hơn.
        score -= (distanceKm / radiusKm).coerceIn(0.0, 1.0) * 10.0

        return score
    }
}
