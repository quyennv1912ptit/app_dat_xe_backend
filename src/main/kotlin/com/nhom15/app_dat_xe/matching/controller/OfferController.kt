package com.nhom15.app_dat_xe.matching.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/offers")
class OfferController {
    @PostMapping("/{id}/accept")
    fun diverAcceptedOffer(@PathVariable id: Long) {

    }

    @PostMapping("/{id}/reject")
    fun diverRejectedOffer(@PathVariable id: Long) {

    }
}