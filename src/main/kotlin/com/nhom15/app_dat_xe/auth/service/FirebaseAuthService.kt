package com.nhom15.app_dat_xe.auth.service

import com.google.firebase.auth.AuthErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.exception.AppException
import org.springframework.stereotype.Service

/** Điểm duy nhất verify Firebase ID Token. Lỗi Firebase được đổi sang ErrorCode chung. */
@Service
class FirebaseAuthService(
    private val firebaseAuth: FirebaseAuth
) {

    fun verifyToken(idToken: String): FirebaseToken =
        try {
            firebaseAuth.verifyIdToken(idToken)
        } catch (e: FirebaseAuthException) {
            val code = if (e.authErrorCode == AuthErrorCode.EXPIRED_ID_TOKEN) {
                ErrorCode.TOKEN_EXPIRED
            } else {
                ErrorCode.TOKEN_INVALID
            }
            throw AppException(code, cause = e)
        } catch (e: IllegalArgumentException) {
            throw AppException(ErrorCode.TOKEN_INVALID, cause = e)
        }
}