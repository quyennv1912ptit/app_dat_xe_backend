package com.nhom15.app_dat_xe.auth.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.nhom15.app_dat_xe.auth.dto.LoginResponse
import com.nhom15.app_dat_xe.auth.entity.Drivers
import com.nhom15.app_dat_xe.auth.entity.Users
import com.nhom15.app_dat_xe.auth.repository.DriversRepository
import com.nhom15.app_dat_xe.auth.repository.UsersRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    private val firebaseAuth: FirebaseAuth,
    private val usersRepository: UsersRepository,
    private val driversRepository: DriversRepository
) {

    @Throws(Exception::class)
    fun verifyToken(idToken: String): FirebaseToken {
        return firebaseAuth.verifyIdToken(idToken)
    }

    @Throws(Exception::class)
    fun login(idToken: String, role: String?): LoginResponse {

        // 1. VERIFY FIREBASE ID TOKEN
        val firebaseToken =
            firebaseAuth.verifyIdToken(idToken)

        val uid = firebaseToken.uid

        println("========== FIREBASE LOGIN ==========")
        println("UID: $uid")
        println("ROLE REQUEST: $role")

        // 2. KIỂM TRA ROLE
        if (role == null ||
            (!role.equals("CUSTOMER", ignoreCase = true)
                    && !role.equals("DRIVER", ignoreCase = true))
        ) {
            throw Exception("Invalid role")
        }

        // 3. CUSTOMER
        if ("CUSTOMER".equals(role, ignoreCase = true)) {

            val userByUid =
                usersRepository.findByFirebaseUid(uid)

            if (userByUid.isPresent) {

                val user = userByUid.get()

                println(
                    "Tìm thấy CUSTOMER bằng Firebase UID"
                )

                return LoginResponse(
                    user.id,
                    uid,
                    user.email,
                    user.phoneNumber,
                    user.fullName,
                    "CUSTOMER"
                )
            }

            throw Exception(
                "CUSTOMER account does not exist in database"
            )
        }

        // 4. DRIVER
        if ("DRIVER".equals(role, ignoreCase = true)) {

            val driverByUid =
                driversRepository.findByFirebaseUid(uid)

            if (driverByUid.isPresent) {

                val driver =
                    driverByUid.get()

                println(
                    "Tìm thấy DRIVER bằng Firebase UID"
                )

                return LoginResponse(
                    driver.id,
                    uid,
                    driver.email,
                    driver.phoneNumber,
                    driver.fullName,
                    "DRIVER"
                )
            }

            throw Exception(
                "DRIVER account does not exist in database"
            )
        }

        throw Exception("Invalid role")
    }

    @Throws(Exception::class)
    fun loginWithFacebook(
        idToken: String,
        role: String?
    ): LoginResponse {

        val firebaseToken =
            firebaseAuth.verifyIdToken(idToken)

        val uid = firebaseToken.uid
        val email = firebaseToken.email
        val name = firebaseToken.name

        println("========== FACEBOOK LOGIN ==========")
        println("FACEBOOK UID: $uid")
        println("FACEBOOK EMAIL: $email")
        println("FACEBOOK NAME: $name")
        println("ROLE: $role")

        // Chỉ kiểm tra xem account Facebook đã tồn tại chưa
        if ("CUSTOMER".equals(role, ignoreCase = true)) {

            val existingUser =
                usersRepository.findByFirebaseUid(uid)

            if (existingUser.isPresent) {

                val user = existingUser.get()

                return LoginResponse(
                    user.id,
                    uid,
                    user.email,
                    user.phoneNumber,
                    user.fullName,
                    "CUSTOMER"
                )
            }

            // Chưa tạo Users ở đây
            return LoginResponse(
                null,
                uid,
                email,
                null,
                name,
                "LINK_PHONE"
            )
        }

        if ("DRIVER".equals(role, ignoreCase = true)) {

            val existingDriver =
                driversRepository.findByFirebaseUid(uid)

            if (existingDriver.isPresent) {

                val driver = existingDriver.get()

                return LoginResponse(
                    driver.id,
                    uid,
                    driver.email,
                    driver.phoneNumber,
                    driver.fullName,
                    "DRIVER"
                )
            }

            // Chưa tạo Drivers ở đây
            return LoginResponse(
                null,
                uid,
                email,
                null,
                name,
                "LINK_PHONE"
            )
        }

        throw Exception("Invalid role")
    }

    @Throws(Exception::class)
    fun linkPhone(
        providerIdToken: String,
        phoneIdToken: String,
        phoneNumber: String,
        role: String?
    ): LoginResponse {

        // 1. VERIFY PROVIDER TOKEN
        val providerToken =
            firebaseAuth.verifyIdToken(providerIdToken)

        val providerUid =
            providerToken.uid

        val email =
            providerToken.email

        val name =
            providerToken.name

        // 2. VERIFY PHONE TOKEN
        val phoneToken =
            firebaseAuth.verifyIdToken(phoneIdToken)

        val tokenPhoneObject =
            phoneToken.claims["phone_number"]

        if (tokenPhoneObject == null) {

            throw Exception(
                "Phone ID Token không chứa số điện thoại"
            )
        }

        val verifiedPhone =
            tokenPhoneObject.toString()

        if (verifiedPhone != phoneNumber) {

            throw Exception(
                "Số điện thoại không khớp với Firebase ID Token"
            )
        }

        // 3. CUSTOMER
        if ("CUSTOMER".equals(role, ignoreCase = true)) {

            // Kiểm tra provider account đã tồn tại chưa
            val existingUser =
                usersRepository.findByFirebaseUid(providerUid)

            if (existingUser.isPresent) {

                val user = existingUser.get()

                // Nếu account đã có SĐT thì đây là login lại
                val currentPhone = user.phoneNumber

                if (currentPhone != null && currentPhone.isNotBlank()) {

                    return LoginResponse(
                        user.id,
                        providerUid,
                        user.email,
                        user.phoneNumber,
                        user.fullName,
                        "CUSTOMER"
                    )
                }

                // Account provider tồn tại nhưng chưa có SĐT
                user.phoneNumber = phoneNumber

                val savedUser =
                    usersRepository.save(user)

                return LoginResponse(
                    savedUser.id,
                    providerUid,
                    savedUser.email,
                    savedUser.phoneNumber,
                    savedUser.fullName,
                    "CUSTOMER"
                )
            }

            // TẠO ACCOUNT PROVIDER
            val newUser = Users()

            newUser.firebaseUid = providerUid
            newUser.email = email
            newUser.fullName = name
            newUser.phoneNumber = phoneNumber
            newUser.createdAt = LocalDateTime.now()

            val savedUser =
                usersRepository.save(newUser)

            return LoginResponse(
                savedUser.id,
                providerUid,
                savedUser.email,
                savedUser.phoneNumber,
                savedUser.fullName,
                "CUSTOMER"
            )
        }

        // 4. DRIVER
        if ("DRIVER".equals(role, ignoreCase = true)) {

            val existingDriver =
                driversRepository.findByFirebaseUid(providerUid)

            if (existingDriver.isPresent) {

                val driver =
                    existingDriver.get()

                val currentPhone = driver.phoneNumber

                if (currentPhone != null && currentPhone.isNotBlank()) {

                    return LoginResponse(
                        driver.id,
                        providerUid,
                        driver.email,
                        driver.phoneNumber,
                        driver.fullName,
                        "DRIVER"
                    )
                }

                driver.phoneNumber = phoneNumber

                val savedDriver =
                    driversRepository.save(driver)

                return LoginResponse(
                    savedDriver.id,
                    providerUid,
                    savedDriver.email,
                    savedDriver.phoneNumber,
                    savedDriver.fullName,
                    "DRIVER"
                )
            }

            // TẠO DRIVER PROVIDER
            val newDriver = Drivers()

            newDriver.firebaseUid = providerUid
            newDriver.email = email
            newDriver.fullName = name
            newDriver.phoneNumber = phoneNumber
            newDriver.createdAt = LocalDateTime.now()

            val savedDriver =
                driversRepository.save(newDriver)

            return LoginResponse(
                savedDriver.id,
                providerUid,
                savedDriver.email,
                savedDriver.phoneNumber,
                savedDriver.fullName,
                "DRIVER"
            )
        }

        throw Exception("Invalid role")
    }

    @Throws(Exception::class)
    fun register(
        idToken: String,
        phoneNumber: String,
        fullName: String,
        email: String?,
        role: String?
    ): LoginResponse {

        val firebaseToken =
            firebaseAuth.verifyIdToken(idToken)

        val uid = firebaseToken.uid

        println("========== REGISTER ==========")
        println("UID: $uid")
        println("PHONE: $phoneNumber")
        println("FULL NAME: $fullName")
        println("EMAIL: $email")
        println("ROLE: $role")

        if (role == null ||
            (!role.equals("CUSTOMER", ignoreCase = true)
                    && !role.equals("DRIVER", ignoreCase = true))
        ) {
            throw Exception("Invalid role")
        }

        // CUSTOMER
        if ("CUSTOMER".equals(role, ignoreCase = true)) {

            // CHỈ kiểm tra Firebase UID
            val existingUser =
                usersRepository.findByFirebaseUid(uid)

            if (existingUser.isPresent) {

                throw Exception(
                    "Tài khoản CUSTOMER đã được đăng ký"
                )
            }

            val newUser = Users()

            newUser.firebaseUid = uid
            newUser.phoneNumber = phoneNumber
            newUser.fullName = fullName
            newUser.email = email
            newUser.createdAt = LocalDateTime.now()

            val savedUser =
                usersRepository.save(newUser)

            println(
                "ĐĂNG KÝ CUSTOMER THÀNH CÔNG"
            )

            return LoginResponse(
                savedUser.id,
                uid,
                savedUser.email,
                savedUser.phoneNumber,
                savedUser.fullName,
                "CUSTOMER"
            )
        }

        // DRIVER
        if ("DRIVER".equals(role, ignoreCase = true)) {

            // CHỈ kiểm tra Firebase UID
            val existingDriver =
                driversRepository.findByFirebaseUid(uid)

            if (existingDriver.isPresent) {

                throw Exception(
                    "Tài khoản DRIVER đã được đăng ký"
                )
            }

            val newDriver = Drivers()

            newDriver.firebaseUid = uid
            newDriver.phoneNumber = phoneNumber
            newDriver.fullName = fullName
            newDriver.email = email
            newDriver.createdAt = LocalDateTime.now()

            val savedDriver =
                driversRepository.save(newDriver)

            println(
                "ĐĂNG KÝ DRIVER THÀNH CÔNG"
            )

            return LoginResponse(
                savedDriver.id,
                uid,
                savedDriver.email,
                savedDriver.phoneNumber,
                savedDriver.fullName,
                "DRIVER"
            )
        }

        throw Exception("Invalid role")
    }

    @Throws(Exception::class)
    fun loginWithGoogle(
        idToken: String,
        role: String?
    ): LoginResponse {

        val decodedToken = verifyToken(idToken)

        val uid = decodedToken.uid
        val email = decodedToken.email
        val name = decodedToken.name

        println("========== GOOGLE LOGIN ==========")
        println("UID = $uid")
        println("EMAIL = $email")
        println("NAME = $name")

        if ("DRIVER".equals(role, ignoreCase = true)) {

            val driver =
                driversRepository.findByFirebaseUid(uid)

            if (driver.isPresent) {

                val d = driver.get()

                // Đã có Google nhưng chưa có số điện thoại
                val currentPhone = d.phoneNumber

                if (currentPhone == null || currentPhone.isBlank()) {

                    return LoginResponse(
                        d.id,
                        uid,
                        email,
                        null,
                        d.fullName,
                        "LINK_PHONE"
                    )
                }

                // Đã có đầy đủ Google + phone
                return LoginResponse(
                    d.id,
                    uid,
                    email,
                    d.phoneNumber,
                    d.fullName,
                    "DRIVER"
                )
            }

        } else {

            val user =
                usersRepository.findByFirebaseUid(uid)

            if (user.isPresent) {

                val u = user.get()

                // Đã có Google nhưng chưa có số điện thoại
                val currentPhone = u.phoneNumber

                if (currentPhone == null || currentPhone.isBlank()) {

                    return LoginResponse(
                        u.id,
                        uid,
                        email,
                        null,
                        u.fullName,
                        "LINK_PHONE"
                    )
                }

                // Đã có đầy đủ Google + phone
                return LoginResponse(
                    u.id,
                    uid,
                    email,
                    u.phoneNumber,
                    u.fullName,
                    "CUSTOMER"
                )
            }
        }

        // Google account hoàn toàn mới
        return LoginResponse(
            null,
            uid,
            email,
            null,
            name,
            "LINK_PHONE"
        )
    }
}