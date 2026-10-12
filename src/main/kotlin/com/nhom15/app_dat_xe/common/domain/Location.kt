package com.nhom15.app_dat_xe.common.domain

import jakarta.persistence.Embeddable

@Embeddable
data class Location(
    val geoPoint: GeoPoint,
    val address: String
)