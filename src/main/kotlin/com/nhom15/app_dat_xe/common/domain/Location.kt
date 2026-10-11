
package com.nhom15.app_dat_xe.common.domain

import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded

@Embeddable
class Location(
    @Embedded
    val geoPoint: GeoPoint,

    val address: String
)