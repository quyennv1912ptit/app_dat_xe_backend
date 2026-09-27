package com.nhom15.app_dat_xe.service

import com.google.firebase.auth.FirebaseAuth
import com.nhom15.app_dat_xe.dto.ProfileResponse
import com.nhom15.app_dat_xe.repository.DriversRepository
import com.nhom15.app_dat_xe.repository.UsersRepository
import org.springframework.stereotype.Service

@Service
class ProfileService(private val firebaseAuth: FirebaseAuth,
    private val usersRepository: UsersRepository, private val driversRepository: DriversRepository) {

    fun getProfile(idToken: String, role: String)
    : ProfileResponse {

        val firebaseToken =
            firebaseAuth.verifyIdToken(idToken)

        val uid = firebaseToken.uid

        if (role.equals("CUSTOMER", ignoreCase = true)) {

            val user = usersRepository
                .findByFirebaseUid(uid)
                .orElseThrow { Exception("CUSTOMER account does not exist") }

            return ProfileResponse(user.id, uid, user.fullName,
                user.email, user.phoneNumber, user.avatarUrl, "CUSTOMER")
        }

        if (role.equals("DRIVER", ignoreCase = true)) {

            val driver = driversRepository.findByFirebaseUid(uid)
                .orElseThrow { Exception("DRIVER account does not exist") }

            return ProfileResponse(driver.id, uid, driver.fullName,
                driver.email, driver.phoneNumber, driver.avatarUrl, "DRIVER")
        }

        throw Exception("Invalid role")
    }

    fun updateAvatar(idToken: String, role: String, avatarUrl: String)
    : ProfileResponse {

        val firebaseToken = firebaseAuth.verifyIdToken(idToken)

        val uid = firebaseToken.uid

        if (role.equals("CUSTOMER", ignoreCase = true)) {

            val user = usersRepository
                .findByFirebaseUid(uid)
                .orElseThrow { Exception("CUSTOMER account does not exist") }

            user.avatarUrl = avatarUrl

            val savedUser = usersRepository.save(user)

            return ProfileResponse(savedUser.id, uid, savedUser.fullName,
                savedUser.email, savedUser.phoneNumber, savedUser.avatarUrl, "CUSTOMER")
        }

        if (role.equals("DRIVER", ignoreCase = true)) {

            val driver = driversRepository
                .findByFirebaseUid(uid)
                .orElseThrow { Exception("DRIVER account does not exist") }

            driver.avatarUrl = avatarUrl

            val savedDriver =
                driversRepository.save(driver)

            return ProfileResponse(savedDriver.id, uid, savedDriver.fullName,
                savedDriver.email, savedDriver.phoneNumber, savedDriver.avatarUrl, "DRIVER")
        }

        throw Exception("Invalid role")
    }
}