package com.nhom15.app_dat_xe.repository;

import com.nhom15.app_dat_xe.entity.Drivers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriversRepository extends JpaRepository<Drivers, Long> {
    public Optional<Drivers> findByPhoneNumber(String phoneNumber);
    public Optional<Drivers> findByFirebaseUid(String firebaseUid);
    public Optional<Drivers> findByEmail(String email);
}
