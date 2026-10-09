package com.nhom15.app_dat_xe.booking.controller

import com.nhom15.app_dat_xe.booking.api.dto.request.BookingListFilter
import com.nhom15.app_dat_xe.booking.api.dto.request.CreateBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.EstimateBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.CancelBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.CompleteBookingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.DriverArrivedRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.DriverStartArrivingRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.CreateHandoverReportRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.ConfirmHandoverRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.StartTripRequest
import com.nhom15.app_dat_xe.booking.api.dto.request.ArriveDestinationRequest
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingStatusResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingDetailResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.BookingSummaryResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.CreateBookingResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.EstimateBookingResponse
import com.nhom15.app_dat_xe.booking.api.dto.response.HandoverReportResponse
import com.nhom15.app_dat_xe.booking.service.BookingHandoverService
import com.nhom15.app_dat_xe.booking.service.BookingQueryService
import com.nhom15.app_dat_xe.booking.service.BookingService
import com.nhom15.app_dat_xe.common.api.ApiResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.api.PageResponse
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.common.security.CurrentUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bookings")
class BookingController(
    private val bookingService: BookingService,
    private val bookingQueryService: BookingQueryService,
    private val bookingHandoverService: BookingHandoverService,
) {
    @PostMapping("/estimate")
    fun estimateBooking(
        @CurrentUser actor: AuthUser?,
        @Valid @RequestBody request: EstimateBookingRequest,
    ): ApiResponse<EstimateBookingResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.estimateBooking(actor, request))
    }

    @GetMapping("/{id}")
    fun getBookingDetail(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
    ): ApiResponse<BookingDetailResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingQueryService.getDetail(actor, id))
    }

    @GetMapping
    fun listMyBookings(
        @CurrentUser actor: AuthUser?,
        @Valid @ModelAttribute filter: BookingListFilter,
    ): ApiResponse<PageResponse<BookingSummaryResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingQueryService.listMyBookings(actor, filter))
    }

    @PostMapping
    fun createBooking(
        @CurrentUser actor: AuthUser,
        @Valid @RequestBody request: CreateBookingRequest,
    ): ResponseEntity<ApiResponse<CreateBookingResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(bookingService.createBooking(actor, request)))

    @PostMapping("/{id}/cancel")
    fun cancelBooking(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: CancelBookingRequest,
    ): ApiResponse<BookingStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.cancelBooking(actor, id, request))
    }

    @PostMapping("/{id}/start-arriving")
    fun startArriving(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: DriverStartArrivingRequest,
    ): ApiResponse<BookingStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.startArriving(actor, id, request))
    }

    @PostMapping("/{id}/arrived")
    fun arrivePickup(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: DriverArrivedRequest,
    ): ApiResponse<BookingStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.arrivePickup(actor, id, request))
    }

    @PostMapping("/{id}/handover/pickup")
    fun createPickupHandover(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateHandoverReportRequest,
    ): ResponseEntity<ApiResponse<HandoverReportResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        val report = bookingHandoverService.createPickup(actor, id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(report))
    }

    @PostMapping("/{id}/handover/pickup/confirm")
    fun confirmPickupHandover(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: ConfirmHandoverRequest,
    ): ApiResponse<HandoverReportResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingHandoverService.confirmPickup(actor, id, request))
    }

    @PostMapping("/{id}/handover/dropoff")
    fun createDropoffHandover(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateHandoverReportRequest,
    ): ResponseEntity<ApiResponse<HandoverReportResponse>> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        val report = bookingHandoverService.createDropoff(actor, id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(report))
    }

    @PostMapping("/{id}/start")
    fun startTrip(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: StartTripRequest,
    ): ApiResponse<BookingStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.startTrip(actor, id, request))
    }

    @PostMapping("/{id}/arrive-destination")
    fun arriveDestination(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: ArriveDestinationRequest,
    ): ApiResponse<BookingStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.arriveDestination(actor, id, request))
    }

    @PostMapping("/{id}/complete")
    fun completeBooking(
        @CurrentUser actor: AuthUser?,
        @PathVariable id: Long,
        @Valid @RequestBody request: CompleteBookingRequest,
    ): ApiResponse<BookingStatusResponse> {
        if (actor == null) {
            throw AppException(ErrorCode.UNAUTHORIZED)
        }
        return ApiResponse.ok(bookingService.completeBooking(actor, id, request))
    }
}
