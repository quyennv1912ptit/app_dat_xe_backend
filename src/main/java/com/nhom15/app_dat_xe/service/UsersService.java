package com.nhom15.app_dat_xe.service;

import com.nhom15.app_dat_xe.entity.Users;
import com.nhom15.app_dat_xe.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Optional<Users> findByPhoneNumber(String phoneNumber){
        return usersRepository.findByPhoneNumber(phoneNumber);
    }
}
