
package com.nhom15.app_dat_xe.notification.token

import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.notification.api.Recipient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DeviceTokenService(
    private val deviceTokenRepository: DeviceTokenRepository
) {

    @Transactional
    fun registerOrUpdate(
        userId: Long,
        role: Role,
        installationId: String,
        fcmToken: String,
        platform: String
    ) {
        val recipientRole = role.toRecipientRole()
        val now = Instant.now()

        val existingByInstallation =
            deviceTokenRepository.findByInstallationId(installationId)

        val existingByToken =
            deviceTokenRepository.findByFcmToken(fcmToken)

        if (existingByInstallation != null) {
            if (
                existingByToken != null &&
                existingByToken.installationId != installationId
            ) {
                deviceTokenRepository.delete(existingByToken)
                deviceTokenRepository.flush()
            }

            existingByInstallation.ownerId = userId
            existingByInstallation.role = recipientRole
            existingByInstallation.fcmToken = fcmToken
            existingByInstallation.platform = platform
            existingByInstallation.lastSeenAt = now

            deviceTokenRepository.save(existingByInstallation)
            return
        }

        if (existingByToken != null) {
            existingByToken.installationId = installationId
            existingByToken.ownerId = userId
            existingByToken.role = recipientRole
            existingByToken.platform = platform
            existingByToken.lastSeenAt = now

            deviceTokenRepository.save(existingByToken)
            return
        }

        deviceTokenRepository.save(
            DeviceToken(
                installationId = installationId,
                ownerId = userId,
                role = recipientRole,
                fcmToken = fcmToken,
                platform = platform,
                lastSeenAt = now
            )
        )
    }

    @Transactional
    fun unregister(
        userId: Long,
        role: Role,
        installationId: String
    ) {
        val recipientRole = role.toRecipientRole()

        val token = deviceTokenRepository
            .findByInstallationIdAndOwnerIdAndRole(
                installationId = installationId,
                ownerId = userId,
                role = recipientRole
            )

        if (token != null) {
            deviceTokenRepository.delete(token)
        }
    }

    private fun Role.toRecipientRole(): Recipient.Role {
        return when (this) {
            Role.CUSTOMER -> Recipient.Role.USER
            Role.DRIVER -> Recipient.Role.DRIVER
            Role.ADMIN -> throw BadRequestException(
                ErrorCode.VALIDATION_ERROR,
                "Admin không hỗ trợ device token"
            )
        }
    }
}
