package com.nhom15.app_dat_xe.common.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * Spring lấy principal từ SecurityContext (chính là AuthUser do FirebaseAuthFilter đặt vào).
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@AuthenticationPrincipal
annotation class CurrentUser