package com.nhom15.app_dat_xe.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.nhom15.app_dat_xe.dto.LoginResponse;
import com.nhom15.app_dat_xe.entity.Drivers;
import com.nhom15.app_dat_xe.entity.Users;
import com.nhom15.app_dat_xe.repository.DriversRepository;
import com.nhom15.app_dat_xe.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final UsersRepository usersRepository;
    private final DriversRepository driversRepository;

    public AuthService(FirebaseAuth firebaseAuth, UsersRepository usersRepository
            , DriversRepository driversRepository
    ) {
        this.firebaseAuth = firebaseAuth;
        this.usersRepository = usersRepository;
        this.driversRepository = driversRepository;
    }

    public FirebaseToken verifyToken(String idToken) throws Exception {
        return firebaseAuth.verifyIdToken(idToken);
    }

    public LoginResponse login(String idToken, String role) throws Exception {

        // 1. VERIFY FIREBASE ID TOKEN
        FirebaseToken firebaseToken =
                firebaseAuth.verifyIdToken(idToken);

        String uid = firebaseToken.getUid();

        System.out.println("========== FIREBASE LOGIN ==========");
        System.out.println("UID: " + uid);
        System.out.println("ROLE REQUEST: " + role);

        // 2. KIỂM TRA ROLE
        if (role == null ||
                (!role.equalsIgnoreCase("CUSTOMER")
                        && !role.equalsIgnoreCase("DRIVER"))) {

            throw new Exception("Invalid role");
        }

        // 3. CUSTOMER
        if ("CUSTOMER".equalsIgnoreCase(role)) {

            var userByUid =
                    usersRepository.findByFirebaseUid(uid);

            if (userByUid.isPresent()) {

                Users user = userByUid.get();

                System.out.println(
                        "Tìm thấy CUSTOMER bằng Firebase UID"
                );

                return new LoginResponse(
                        user.getId(),
                        uid,
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getFullName(),
                        "CUSTOMER"
                );
            }

            throw new Exception(
                    "CUSTOMER account does not exist in database"
            );
        }

        // 4. DRIVER
        if ("DRIVER".equalsIgnoreCase(role)) {

            var driverByUid =
                    driversRepository.findByFirebaseUid(uid);

            if (driverByUid.isPresent()) {

                Drivers driver =
                        driverByUid.get();

                System.out.println(
                        "Tìm thấy DRIVER bằng Firebase UID"
                );

                return new LoginResponse(
                        driver.getId(),
                        uid,
                        driver.getEmail(),
                        driver.getPhoneNumber(),
                        driver.getFullName(),
                        "DRIVER"
                );
            }

            throw new Exception(
                    "DRIVER account does not exist in database"
            );
        }


        throw new Exception("Invalid role");
    }

    public LoginResponse loginWithFacebook(String idToken, String role) throws Exception {

        FirebaseToken firebaseToken =
                firebaseAuth.verifyIdToken(idToken);

        String uid = firebaseToken.getUid();
        String email = firebaseToken.getEmail();
        String name = firebaseToken.getName();

        System.out.println("========== FACEBOOK LOGIN ==========");
        System.out.println("FACEBOOK UID: " + uid);
        System.out.println("FACEBOOK EMAIL: " + email);
        System.out.println("FACEBOOK NAME: " + name);
        System.out.println("ROLE: " + role);

        // Chỉ kiểm tra xem account Facebook đã tồn tại chưa
        if ("CUSTOMER".equalsIgnoreCase(role)) {

            var existingUser =
                    usersRepository.findByFirebaseUid(uid);

            if (existingUser.isPresent()) {

                Users user = existingUser.get();

                return new LoginResponse(
                        user.getId(),
                        uid,
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getFullName(),
                        "CUSTOMER"
                );
            }

            // Chưa tạo Users ở đây
            return new LoginResponse(
                    null,
                    uid,
                    email,
                    null,
                    name,
                    "LINK_PHONE"
            );
        }

        if ("DRIVER".equalsIgnoreCase(role)) {

            var existingDriver =
                    driversRepository.findByFirebaseUid(uid);

            if (existingDriver.isPresent()) {

                Drivers driver = existingDriver.get();

                return new LoginResponse(
                        driver.getId(),
                        uid,
                        driver.getEmail(),
                        driver.getPhoneNumber(),
                        driver.getFullName(),
                        "DRIVER"
                );
            }

            // Chưa tạo Drivers ở đây
            return new LoginResponse(
                    null,
                    uid,
                    email,
                    null,
                    name,
                    "LINK_PHONE"
            );
        }

        throw new Exception("Invalid role");
    }

    public LoginResponse linkPhone(String providerIdToken, String phoneIdToken,
            String phoneNumber, String role
    ) throws Exception {

        // 1. VERIFY PROVIDER TOKEN
        FirebaseToken providerToken =
                firebaseAuth.verifyIdToken(providerIdToken);

        String providerUid =
                providerToken.getUid();

        String email =
                providerToken.getEmail();

        String name =
                providerToken.getName();

        // 2. VERIFY PHONE TOKEN
        FirebaseToken phoneToken =
                firebaseAuth.verifyIdToken(phoneIdToken);

        Object tokenPhoneObject =
                phoneToken.getClaims().get("phone_number");

        if (tokenPhoneObject == null) {

            throw new Exception(
                    "Phone ID Token không chứa số điện thoại"
            );
        }

        String verifiedPhone =
                tokenPhoneObject.toString();

        if (!verifiedPhone.equals(phoneNumber)) {

            throw new Exception(
                    "Số điện thoại không khớp với Firebase ID Token"
            );
        }

        // 3. CUSTOMER
        if ("CUSTOMER".equalsIgnoreCase(role)) {

            // Kiểm tra provider account đã tồn tại chưa
            var existingUser =
                    usersRepository.findByFirebaseUid(providerUid);

            if (existingUser.isPresent()) {

                Users user = existingUser.get();

                // Nếu account đã có SĐT thì đây là login lại
                if (user.getPhoneNumber() != null
                        && !user.getPhoneNumber().isBlank()) {

                    return new LoginResponse(
                            user.getId(),
                            providerUid,
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getFullName(),
                            "CUSTOMER"
                    );
                }

                // Account provider tồn tại nhưng chưa có SĐT
                user.setPhoneNumber(phoneNumber);

                Users savedUser =
                        usersRepository.save(user);

                return new LoginResponse(
                        savedUser.getId(),
                        providerUid,
                        savedUser.getEmail(),
                        savedUser.getPhoneNumber(),
                        savedUser.getFullName(),
                        "CUSTOMER"
                );
            }
            // TẠO ACCOUNT PROVIDER

            Users newUser = new Users();

            newUser.setFirebaseUid(providerUid);
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPhoneNumber(phoneNumber);
            newUser.setCreatedAt(LocalDateTime.now());

            Users savedUser =
                    usersRepository.save(newUser);

            return new LoginResponse(
                    savedUser.getId(),
                    providerUid,
                    savedUser.getEmail(),
                    savedUser.getPhoneNumber(),
                    savedUser.getFullName(),
                    "CUSTOMER"
            );
        }

        // 4. DRIVER
        if ("DRIVER".equalsIgnoreCase(role)) {

            var existingDriver =
                    driversRepository.findByFirebaseUid(providerUid);

            if (existingDriver.isPresent()) {

                Drivers driver =
                        existingDriver.get();

                if (driver.getPhoneNumber() != null
                        && !driver.getPhoneNumber().isBlank()) {

                    return new LoginResponse(
                            driver.getId(),
                            providerUid,
                            driver.getEmail(),
                            driver.getPhoneNumber(),
                            driver.getFullName(),
                            "DRIVER"
                    );
                }

                driver.setPhoneNumber(phoneNumber);

                Drivers savedDriver =
                        driversRepository.save(driver);

                return new LoginResponse(
                        savedDriver.getId(),
                        providerUid,
                        savedDriver.getEmail(),
                        savedDriver.getPhoneNumber(),
                        savedDriver.getFullName(),
                        "DRIVER"
                );
            }

            // TẠO DRIVER PROVIDER
            Drivers newDriver = new Drivers();

            newDriver.setFirebaseUid(providerUid);
            newDriver.setEmail(email);
            newDriver.setFullName(name);
            newDriver.setPhoneNumber(phoneNumber);
            newDriver.setCreatedAt(LocalDateTime.now());

            Drivers savedDriver =
                    driversRepository.save(newDriver);

            return new LoginResponse(
                    savedDriver.getId(),
                    providerUid,
                    savedDriver.getEmail(),
                    savedDriver.getPhoneNumber(),
                    savedDriver.getFullName(),
                    "DRIVER"
            );
        }

        throw new Exception("Invalid role");
    }

    public LoginResponse register(String idToken, String phoneNumber,
            String fullName, String email, String role
    ) throws Exception {

        FirebaseToken firebaseToken =
                firebaseAuth.verifyIdToken(idToken);

        String uid = firebaseToken.getUid();
        System.out.println("========== REGISTER ==========");
        System.out.println("UID: " + uid);
        System.out.println("PHONE: " + phoneNumber);
        System.out.println("FULL NAME: " + fullName);
        System.out.println("EMAIL: " + email);
        System.out.println("ROLE: " + role);


        if (role == null ||
                (!role.equalsIgnoreCase("CUSTOMER")
                        && !role.equalsIgnoreCase("DRIVER"))) {

            throw new Exception("Invalid role");
        }

        // CUSTOMER
        if ("CUSTOMER".equalsIgnoreCase(role)) {

            // CHỈ kiểm tra Firebase UID
            var existingUser =
                    usersRepository.findByFirebaseUid(uid);

            if (existingUser.isPresent()) {

                throw new Exception(
                        "Tài khoản CUSTOMER đã được đăng ký"
                );
            }


            Users newUser = new Users();

            newUser.setFirebaseUid(uid);
            newUser.setPhoneNumber(phoneNumber);
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setCreatedAt(LocalDateTime.now());

            Users savedUser =
                    usersRepository.save(newUser);

            System.out.println(
                    "ĐĂNG KÝ CUSTOMER THÀNH CÔNG"
            );

            return new LoginResponse(
                    savedUser.getId(),
                    uid,
                    savedUser.getEmail(),
                    savedUser.getPhoneNumber(),
                    savedUser.getFullName(),
                    "CUSTOMER"
            );
        }

        // DRIVER
        if ("DRIVER".equalsIgnoreCase(role)) {

            // CHỈ kiểm tra Firebase UID
            var existingDriver =
                    driversRepository.findByFirebaseUid(uid);

            if (existingDriver.isPresent()) {

                throw new Exception(
                        "Tài khoản DRIVER đã được đăng ký"
                );
            }


            Drivers newDriver = new Drivers();

            newDriver.setFirebaseUid(uid);
            newDriver.setPhoneNumber(phoneNumber);
            newDriver.setFullName(fullName);
            newDriver.setEmail(email);
            newDriver.setCreatedAt(LocalDateTime.now());

            Drivers savedDriver =
                    driversRepository.save(newDriver);

            System.out.println(
                    "ĐĂNG KÝ DRIVER THÀNH CÔNG"
            );

            return new LoginResponse(
                    savedDriver.getId(),
                    uid,
                    savedDriver.getEmail(),
                    savedDriver.getPhoneNumber(),
                    savedDriver.getFullName(),
                    "DRIVER"
            );
        }


        throw new Exception("Invalid role");
    }

    public LoginResponse loginWithGoogle(String idToken, String role) throws Exception {

        FirebaseToken decodedToken = verifyToken(idToken);

        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String name = decodedToken.getName();
        System.out.println("========== GOOGLE LOGIN ==========");
        System.out.println("UID = " + uid);
        System.out.println("EMAIL = " + email);
        System.out.println("NAME = " + name);

        if ("DRIVER".equalsIgnoreCase(role)) {

            var driver = driversRepository.findByFirebaseUid(uid);

            if (driver.isPresent()) {

                Drivers d = driver.get();

                // Đã có Google nhưng chưa có số điện thoại
                if (d.getPhoneNumber() == null
                        || d.getPhoneNumber().isBlank()) {

                    return new LoginResponse(
                            d.getId(),
                            uid,
                            email,
                            null,
                            d.getFullName(),
                            "LINK_PHONE"
                    );
                }

                // Đã có đầy đủ Google + phone
                return new LoginResponse(
                        d.getId(),
                        uid,
                        email,
                        d.getPhoneNumber(),
                        d.getFullName(),
                        "DRIVER"
                );
            }

        } else {

            var user = usersRepository.findByFirebaseUid(uid);

            if (user.isPresent()) {

                Users u = user.get();

                // Đã có Google nhưng chưa có số điện thoại
                if (u.getPhoneNumber() == null
                        || u.getPhoneNumber().isBlank()) {

                    return new LoginResponse(
                            u.getId(),
                            uid,
                            email,
                            null,
                            u.getFullName(),
                            "LINK_PHONE"
                    );
                }

                // Đã có đầy đủ Google + phone
                return new LoginResponse(
                        u.getId(),
                        uid,
                        email,
                        u.getPhoneNumber(),
                        u.getFullName(),
                        "CUSTOMER"
                );
            }
        }

        // Google account hoàn toàn mới
        return new LoginResponse(
                null,
                uid,
                email,
                null,
                name,
                "LINK_PHONE"
        );
    }
}