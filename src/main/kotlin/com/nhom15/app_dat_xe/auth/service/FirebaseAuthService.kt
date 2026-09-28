package com.nhom15.app_dat_xe.auth.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.stereotype.Service

@Service
class FirebaseAuthService {

    @Throws(Exception::class)
    fun verifyToken(idToken: String): FirebaseToken {
        return FirebaseAuth.getInstance().verifyIdToken(idToken)
    }
}