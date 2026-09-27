package com.nhom15.app_dat_xe.auth.controller

import com.nhom15.app_dat_xe.auth.dto.LinkPhoneRequest
import com.nhom15.app_dat_xe.auth.dto.LoginRequest
import com.nhom15.app_dat_xe.auth.dto.LoginResponse
import com.nhom15.app_dat_xe.auth.dto.RegisterRequest
import com.nhom15.app_dat_xe.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("BACKEND OK")
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest?
    ): ResponseEntity<*> {

        println("ANDROID LOGIN")

        if (request == null ||
            request.idToken == null ||
            request.idToken!!.isBlank()
        ) {
            println("Firebase ID Token bị rỗng")
            return ResponseEntity
                .badRequest()
                .body("Firebase ID Token is empty")
        }

        try {

            val response: LoginResponse =
                authService.login(
                    request.idToken!!,
                    request.role
                )

            println("LOGIN SUCCESS")
            println("UID: ${response.uid}")
            println("ROLE: ${response.role}")

            return ResponseEntity.ok(response)

        } catch (e: Exception) {

            println("LOGIN ERROR")
            println("TYPE: ${e.javaClass.name}")
            println("MESSAGE: ${e.message}")

            e.printStackTrace()

            return ResponseEntity
                .status(401)
                .body(e.message)
        }
    }

    @PostMapping("/facebook")
    fun facebookLogin(
        @RequestBody request: LoginRequest?
    ): ResponseEntity<*> {

        if (request == null ||
            request.idToken == null ||
            request.idToken!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Firebase ID Token is empty")
        }

        try {

            val response: LoginResponse =
                authService.loginWithFacebook(
                    request.idToken!!,
                    request.role
                )

            return ResponseEntity.ok(response)

        } catch (e: Exception) {

            e.printStackTrace()

            return ResponseEntity
                .status(401)
                .body(e.message)
        }
    }

    @PostMapping("/link-phone")
    fun linkPhone(
        @RequestBody request: LinkPhoneRequest?
    ): ResponseEntity<*> {

        if (request == null ||
            request.providerIdToken == null ||
            request.providerIdToken!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Provider ID Token is empty")
        }

        if (request.phoneIdToken == null ||
            request.phoneIdToken!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Phone ID Token is empty")
        }

        if (request.phoneNumber == null ||
            request.phoneNumber!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Phone number is empty")
        }

        try {

            val response: LoginResponse =
                authService.linkPhone(
                    request.providerIdToken!!,
                    request.phoneIdToken!!,
                    request.phoneNumber!!,
                    request.role
                )

            return ResponseEntity.ok(response)

        } catch (e: Exception) {

            e.printStackTrace()

            return ResponseEntity
                .status(401)
                .body(e.message)
        }
    }

    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest?
    ): ResponseEntity<*> {

        if (request == null ||
            request.idToken == null ||
            request.idToken!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Firebase ID Token is empty")
        }

        if (request.phoneNumber == null ||
            request.phoneNumber!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Phone number is empty")
        }

        if (request.fullName == null ||
            request.fullName!!.isBlank()
        ) {

            return ResponseEntity
                .badRequest()
                .body("Full name is empty")
        }

        if (request.role == null ||
            (!request.role!!.equals("CUSTOMER") &&
                    !request.role!!.equals("DRIVER"))
        ) {

            return ResponseEntity
                .badRequest()
                .body("Role must be CUSTOMER or DRIVER")
        }

        try {

            val response: LoginResponse =
                authService.register(
                    request.idToken!!,
                    request.phoneNumber!!,
                    request.fullName!!,
                    request.email,
                    request.role!!
                )

            return ResponseEntity.ok(response)

        } catch (e: Exception) {

            e.printStackTrace()

            return ResponseEntity
                .status(400)
                .body(e.message)
        }
    }

    @PostMapping("/google")
    fun googleLogin(
        @RequestBody request: LoginRequest
    ): ResponseEntity<*> {

        try {

            return ResponseEntity.ok(
                authService.loginWithGoogle(
                    request.idToken!!,
                    request.role
                )
            )

        } catch (e: Exception) {

            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.message)
        }
    }
}