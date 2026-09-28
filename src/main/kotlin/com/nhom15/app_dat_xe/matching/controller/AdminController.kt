package com.nhom15.app_dat_xe.matching.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController {
    @PostMapping("/bookings/{id}/assign")
    fun AdminAssignDriver(@PathVariable id: String, @RequestBody data: String) {

    }

    @GetMapping("/matching/attempts")
    fun getAssignedAttempts() {

    }

}