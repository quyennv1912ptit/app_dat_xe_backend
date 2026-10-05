package com.nhom15.app_dat_xe.auth.service

import com.google.firebase.auth.FirebaseToken
import com.nhom15.app_dat_xe.auth.dto.LoginResponse
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.ConflictException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val firebaseAuthService: FirebaseAuthService,
    private val accountService: AccountService
) {

    fun login(idToken: String, roleStr: String?): LoginResponse {
        val role = parseRole(roleStr)
        val firebaseToken = firebaseAuthService.verifyToken(idToken)
        val uid = firebaseToken.uid

        val account = accountService.findByUid(uid, role)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản $role với uid này")

        return account.toLoginResponse()
    }

    fun loginWithFacebook(idToken: String, roleStr: String?): LoginResponse {
        val role = parseRole(roleStr)
        val firebaseToken = firebaseAuthService.verifyToken(idToken)
        val uid = firebaseToken.uid

        val existingAccount = accountService.findByUid(uid, role)
        if (existingAccount != null) {
            return existingAccount.toLoginResponse()
        }

        // Tài khoản mới từ Facebook, chưa có số điện thoại
        return LoginResponse(
            id = null,
            uid = uid,
            email = firebaseToken.email,
            phoneNumber = null,
            fullName = firebaseToken.name,
            role = "LINK_PHONE"
        )
    }

    fun loginWithGoogle(idToken: String, roleStr: String?): LoginResponse {
        val role = parseRole(roleStr)
        val decodedToken = firebaseAuthService.verifyToken(idToken)
        val uid = decodedToken.uid

        val existingAccount = accountService.findByUid(uid, role)
        if (existingAccount != null) {
            return if (existingAccount.hasPhone) {
                existingAccount.toLoginResponse()
            } else {
                LoginResponse(
                    id = existingAccount.id,
                    uid = uid,
                    email = existingAccount.email,
                    phoneNumber = null,
                    fullName = existingAccount.fullName,
                    role = "LINK_PHONE"
                )
            }
        }

        // Google account hoàn toàn mới
        return LoginResponse(
            id = null,
            uid = uid,
            email = decodedToken.email,
            phoneNumber = null,
            fullName = decodedToken.name,
            role = "LINK_PHONE"
        )
    }

    fun linkPhone(
        providerIdToken: String,
        phoneIdToken: String,
        phoneNumber: String,
        roleStr: String?
    ): LoginResponse {
        val role = parseRole(roleStr)

        // 1. Verify token mạng xã hội (Google/Facebook)
        val providerToken = firebaseAuthService.verifyToken(providerIdToken)
        val providerUid = providerToken.uid

        // 2. Verify token số điện thoại
        val phoneToken = firebaseAuthService.verifyToken(phoneIdToken)
        val verifiedPhone = phoneToken.claims["phone_number"]?.toString()
            ?: throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Phone ID Token không chứa số điện thoại")

        if (verifiedPhone != phoneNumber) {
            throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Số điện thoại không khớp với Firebase ID Token")
        }

        // 3. Liên kết hoặc tạo mới
        val existingAccount = accountService.findByUid(providerUid, role)
        return if (existingAccount != null) {
            if (existingAccount.hasPhone) {
                existingAccount.toLoginResponse()
            } else {
                // Đã có account nhưng thiếu số đt -> cập nhật
                accountService.updatePhone(existingAccount.id, role, phoneNumber).toLoginResponse()
            }
        } else {
            // Chưa có account -> tạo mới
            accountService.create(
                uid = providerUid,
                role = role,
                fullName = providerToken.name,
                email = providerToken.email,
                phoneNumber = phoneNumber
            ).toLoginResponse()
        }
    }

    fun register(
        idToken: String,
        phoneNumber: String,
        fullName: String,
        email: String?,
        roleStr: String?
    ): LoginResponse {
        val role = parseRole(roleStr)
        val firebaseToken = firebaseAuthService.verifyToken(idToken)
        val uid = firebaseToken.uid

        if (accountService.findByUid(uid, role) != null) {
            throw ConflictException(ErrorCode.CONFLICT, "Tài khoản $role đã được đăng ký")
        }

        val newAccount = accountService.create(
            uid = uid,
            role = role,
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber
        )

        return newAccount.toLoginResponse()
    }

    // --- Helper Methods ---

    private fun parseRole(roleStr: String?): Role {
        if (roleStr.isNullOrBlank()) {
            throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Role không được để trống")
        }
        return try {
            Role.valueOf(roleStr.trim().uppercase())
        } catch (e: IllegalArgumentException) {
            throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Role không hợp lệ (Phải là CUSTOMER hoặc DRIVER)")
        }
    }

    private fun Account.toLoginResponse() = LoginResponse(
        id = id,
        uid = firebaseUid,
        email = email,
        phoneNumber = phoneNumber,
        fullName = fullName,
        role = role.name
    )
}