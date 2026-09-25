package com.nhom15.app_dat_xe.repository;

import com.nhom15.app_dat_xe.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByPhoneNumber(String phoneNumber);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByFirebaseUid(String firebaseUid);
}
