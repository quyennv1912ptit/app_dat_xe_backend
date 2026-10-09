package com.nhom15.app_dat_xe.booking.controller

import com.nhom15.app_dat_xe.booking.api.dto.request.BookingListFilter
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingSummaryResponse
import com.nhom15.app_dat_xe.booking.service.BookingQueryService
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.api.PageResponse
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/bookings")
class AdminBookingController(
    private val bookingQueryService: BookingQueryService,
) {
    @GetMapping
    fun listBookings(
        @CurrentUser actor: AuthUser?,
        @Valid @ModelAttribute filter: BookingListFilter,
    ): ApiResponse<PageResponse<BookingSummaryResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingQueryService.listAdminBookings(actor, filter))
    }
}
