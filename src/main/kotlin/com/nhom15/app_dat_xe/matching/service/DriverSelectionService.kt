package com.nhom15.app_dat_xe.matching.service

import com.nhom15.app_dat_xe.account.entity.Drivers
import org.springframework.stereotype.Service
import com.nhom15.app_dat_xe.account.repository.DriversRepository
import com.nhom15.app_dat_xe.common.enums.DriverStatus
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.matching.dto.RankedDriverDTO
import com.nhom15.app_dat_xe.tracking.dto.NearbyDriver

@Service
class DriverSelectionService(
    private val driverRepository: DriversRepository
) {
    fun filterAndRankDrivers(
        nearbyDrivers: List<NearbyDriver>,
        requiredTransmission: Transmission
    ): List<RankedDriverDTO> {
        if (nearbyDrivers.isEmpty()) return emptyList()

        // Tạo map id -> driver
        val driversById: Map<Long, Drivers> = driverRepository
            .findAllById(nearbyDrivers.map { it.driverId })
            .associateBy { it.id }

        return nearbyDrivers
            .mapNotNull { nearby ->
                val driver = driversById[nearby.driverId] ?: return@mapNotNull null
                if (!isEligible(driver, requiredTransmission)) return@mapNotNull null

                RankedDriverDTO(
                    driverId = nearby.driverId,
                    distance = nearby.distanceKm,
                    score = calculateScore(driver, nearby.distanceKm)
                )
            }
            .sortedByDescending { it.score }
    }

    fun isEligible(driver: Drivers, requiredTransmission: Transmission): Boolean {
        if (driver.status != DriverStatus.ONLINE) return false

        if (requiredTransmission == Transmission.MANUAL && driver.licenseClass == "B1") return false

        return true
    }

    private fun calculateScore(driver: Drivers, distanceKm: Double): Double {
        val rating = driver.averageRating
        val experience = driver.yearsOfExperience ?: 0

        var score = 0.0

        score += rating * 10.0

        score += (experience * 2.0).coerceAtMost(10.0)

        score -= distanceKm * 5.0

        return score
    }
}