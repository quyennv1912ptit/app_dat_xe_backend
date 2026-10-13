
package com.nhom15.app_dat_xe.notification.token

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/device-tokens")
class DeviceTokenController(
    private val deviceTokenService: DeviceTokenService
) {

    /**
     * Đăng ký hoặc cập nhật FCM token của thiết bị.
     */
    @PostMapping
    fun addDeviceToken(
        @CurrentUser user: AuthUser,
        @Valid @RequestBody request: DeviceTokenRequest
    ): ApiResponse<String> {
        deviceTokenService.registerOrUpdate(
            userId = user.id,
            role = user.role,
            installationId = request.installationId,
            fcmToken = request.fcmToken,
            platform = request.platform
        )

        return ApiResponse.ok("Đăng ký device token thành công")
    }

    /**
     * Hủy đăng ký FCM token của thiết bị hiện tại.
     */
    @DeleteMapping("/{installationId}")
    fun deleteDeviceToken(
        @CurrentUser user: AuthUser,
        @PathVariable installationId: String
    ): ApiResponse<String> {
        deviceTokenService.unregister(
            userId = user.id,
            role = user.role,
            installationId = installationId
        )

        return ApiResponse.ok("Hủy đăng ký device token thành công")
    }
}
