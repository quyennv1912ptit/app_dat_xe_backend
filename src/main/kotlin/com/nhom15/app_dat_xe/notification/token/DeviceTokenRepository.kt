
package com.nhom15.app_dat_xe.notification.token

import com.nhom15.app_dat_xe.notification.api.Recipient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {

    fun findByOwnerId(ownerId: Long): List<DeviceToken>

    fun findByRoleAndOwnerId(
        role: Recipient.Role,
        ownerId: Long
    ): List<DeviceToken>

    fun findByFcmToken(fcmToken: String): DeviceToken?

    fun findByInstallationId(
        installationId: String
    ): DeviceToken?

    fun findByInstallationIdAndOwnerIdAndRole(
        installationId: String,
        ownerId: Long,
        role: Recipient.Role
    ): DeviceToken?
}
