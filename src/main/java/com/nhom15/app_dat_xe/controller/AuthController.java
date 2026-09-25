package com.nhom15.app_dat_xe.controller;

import com.nhom15.app_dat_xe.dto.LinkPhoneRequest;
import com.nhom15.app_dat_xe.dto.LoginRequest;
import com.nhom15.app_dat_xe.dto.LoginResponse;
import com.nhom15.app_dat_xe.dto.RegisterRequest;
import com.nhom15.app_dat_xe.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("BACKEND OK");
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        System.out.println("ANDROID LOGIN");

        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            System.out.println("Firebase ID Token bị rỗng");
            return ResponseEntity
                    .badRequest()
                    .body("Firebase ID Token is empty");
        }

        try {

            LoginResponse response =
                    authService.login(request.getIdToken(), request.getRole());

            System.out.println("LOGIN SUCCESS");
            System.out.println("UID: " + response.getUid());
            System.out.println("ROLE: " + response.getRole());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            System.out.println("LOGIN ERROR");
            System.out.println("TYPE: " + e.getClass().getName());
            System.out.println("MESSAGE: " + e.getMessage());

            e.printStackTrace();

            return ResponseEntity
                    .status(401)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/facebook")
    public ResponseEntity<?> facebookLogin(
            @RequestBody LoginRequest request
    ) {

        if (request == null ||
                request.getIdToken() == null ||
                request.getIdToken().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Firebase ID Token is empty");
        }

        try {

            LoginResponse response =
                    authService.loginWithFacebook(
                            request.getIdToken(),
                            request.getRole()
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(401)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/link-phone")
    public ResponseEntity<?> linkPhone(
            @RequestBody LinkPhoneRequest request
    ) {

        if (request == null ||
                request.getProviderIdToken() == null ||
                request.getProviderIdToken().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Provider ID Token is empty");
        }

        if (request.getPhoneIdToken() == null ||
                request.getPhoneIdToken().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone ID Token is empty");
        }

        if (request.getPhoneNumber() == null ||
                request.getPhoneNumber().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number is empty");
        }

        try {

            LoginResponse response =
                    authService.linkPhone(
                            request.getProviderIdToken(),
                            request.getPhoneIdToken(),
                            request.getPhoneNumber(),
                            request.getRole()
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(401)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {

        if (request == null ||
                request.getIdToken() == null ||
                request.getIdToken().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Firebase ID Token is empty");
        }

        if (request.getPhoneNumber() == null ||
                request.getPhoneNumber().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number is empty");
        }

        if (request.getFullName() == null ||
                request.getFullName().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Full name is empty");
        }

        if (request.getRole() == null ||
                (!request.getRole().equals("CUSTOMER") &&
                        !request.getRole().equals("DRIVER"))) {

            return ResponseEntity
                    .badRequest()
                    .body("Role must be CUSTOMER or DRIVER");
        }

        try {

            LoginResponse response =
                    authService.register(
                            request.getIdToken(),
                            request.getPhoneNumber(),
                            request.getFullName(),
                            request.getEmail(),
                            request.getRole()
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(400)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(
                    authService.loginWithGoogle(
                            request.getIdToken(),
                            request.getRole()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }
}