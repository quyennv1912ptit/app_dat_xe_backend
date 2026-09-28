package com.nhom15.app_dat_xe.matching.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/driver/me")
class DriverController {
    @PatchMapping("/availability")
    fun changeDriverAvaibility() {

    }

    @GetMapping("/offers")
    fun getWaitingOffers() {

    }
}