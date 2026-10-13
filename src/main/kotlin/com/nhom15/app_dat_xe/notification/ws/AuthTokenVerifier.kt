package com.nhom15.app_dat_xe.notification.ws

import com.nhom15.app_dat_xe.notification.api.Recipient


interface AuthTokenVerifier {
    fun verify(token: String): Recipient?
}