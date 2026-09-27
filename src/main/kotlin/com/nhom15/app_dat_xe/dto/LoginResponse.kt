package com.nhom15.app_dat_xe.dto

class LoginResponse {

    var id: Long? = null
    var uid: String? = null
    var email: String? = null
    var phoneNumber: String? = null
    var fullName: String? = null
    var role: String? = null

    constructor()

    constructor(id: Long?, uid: String?, email: String?,
        phoneNumber: String?, fullName: String?, role: String?) {
        this.id = id
        this.uid = uid
        this.email = email
        this.phoneNumber = phoneNumber
        this.fullName = fullName
        this.role = role
    }
}