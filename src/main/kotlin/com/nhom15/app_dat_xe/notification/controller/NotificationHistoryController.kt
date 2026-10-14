package com.nhom15.app_dat_xe.notification.controller

import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import com.nhom15.app_dat_xe.notification.dto.NotificationHistoryItem
import com.nhom15.app_dat_xe.notification.service.NotificationHistoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
class NotificationHistoryController(private val historyService: NotificationHistoryService) {
    @GetMapping
    fun list(@CurrentUser actor: AuthUser): ApiResponse<List<NotificationHistoryItem>> =
        ApiResponse.ok(historyService.list(actor))
}
