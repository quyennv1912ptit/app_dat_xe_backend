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
            .mapNotNull { d -> d.id?.let { id -> id to d } }
            .toMap()

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
        if (driver.licenseVerified != true) return false
        if (driver.isFlagged == true) return false
        if (requiredTransmission == Transmission.MANUAL && driver.licenseClass == "B1") return false
        return true
    }

    private fun calculateScore(driver: Drivers, distanceKm: Double): Double {
        val rating = driver.averageRating ?: 5.0
        val completion = driver.completionRate ?: 1.0
        val acceptance = driver.acceptanceRate ?: 1.0
        val cancelRate = driver.cancelRate30d ?: 0.0
        val experience = driver.yearsOfExperience ?: 0
        val risk = driver.riskScore ?: 0.0

        var score = 0.0

        score += rating * 10.0
        score += completion * 20.0
        score += acceptance * 10.0

        score += (experience * 2.0).coerceAtMost(10.0)

        score -= cancelRate * 30.0
        score -= risk * 10.0
        score -= distanceKm * 5.0

        return score
    }

}