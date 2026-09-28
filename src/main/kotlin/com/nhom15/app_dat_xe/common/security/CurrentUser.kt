package com.nhom15.app_dat_xe.common.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * Dùng ở controller để lấy người dùng hiện tại:
 *
 *   @GetMapping("/bookings")
 *   fun myBookings(@CurrentUser user: AuthUser) = ...
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@AuthenticationPrincipal
annotation class CurrentUser
