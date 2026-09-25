package com.nhom15.app_dat_xe.service;

import com.nhom15.app_dat_xe.entity.Drivers;
import com.nhom15.app_dat_xe.repository.DriversRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriversService {
    private final DriversRepository driversRepository;

    public DriversService(DriversRepository driversRepository) {
        this.driversRepository = driversRepository;
    }
    public Optional<Drivers> findByPhoneNumber(String phongNumber){
        return driversRepository.findByPhoneNumber(phongNumber);
    }
}
