package com.nhom15.app_dat_xe.auth.service

import com.nhom15.app_dat_xe.account.repository.DriversRepository
import com.nhom15.app_dat_xe.account.repository.UsersRepository
import com.nhom15.app_dat_xe.notification.api.Recipient
import com.nhom15.app_dat_xe.notification.ws.AuthTokenVerifier
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

/**
 * Cầu nối: FirebaseAuthService (verify token) -> Recipient (người dùng trong hệ thống đặt xe).
 *
 * FirebaseAuthService.verifyToken trả về FirebaseToken, nó chỉ chứa uid của Firebase.
 * Interceptor cần biết đó là khách hay tài xế và id trong DB của mình, nên phải tra DB một lần ở đây.
 */
@Component
class FirebaseTokenVerifier(
    private val firebaseAuthService: FirebaseAuthService,
    private val usersRepository: UsersRepository,
    private val driversRepository: DriversRepository
) : AuthTokenVerifier {

    override fun verify(token: String): Recipient? {
        val uid = firebaseAuthService.verifyToken(token).uid

        usersRepository.findByFirebaseUid(uid).getOrNull()?.let { return Recipient.User(it.id) }
        driversRepository.findByFirebaseUid(uid).getOrNull()?.let { return Recipient.Driver(it.id) }
        return null
    }
}