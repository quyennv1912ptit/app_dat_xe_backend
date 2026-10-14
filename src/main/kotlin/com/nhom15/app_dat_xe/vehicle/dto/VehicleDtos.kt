package com.nhom15.app_dat_xe.vehicle.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.nhom15.app_dat_xe.common.enums.Transmission
import com.nhom15.app_dat_xe.common.enums.VehicleType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateVehicleRequest(
    @JsonProperty("plate_number")
    @JsonAlias("plateNumber")
    @field:NotBlank
    @field:Size(max = 30)
    val plateNumber: String,

    @JsonProperty("transmission_type")
    @JsonAlias("transmissionType")
    @field:NotNull
    val transmissionType: Transmission,

    @JsonProperty("vehicle_type")
    @JsonAlias("vehicleType")
    @field:NotNull
    val vehicleType: VehicleType,

    @JsonProperty("brand")
    @JsonAlias("make", "manufacturer")
    @field:Size(max = 100)
    val brand: String? = null,

    @field:Size(max = 100)
    val model: String? = null,

    @field:Size(max = 50)
    val color: String? = null,
)

data class VehicleResponse(
    val id: Long,
    @JsonProperty("plate_number") val plateNumber: String?,
    @JsonProperty("transmission_type") val transmissionType: String?,
    @JsonProperty("vehicle_type") val vehicleType: String?,
    val brand: String?,
    val model: String?,
    val color: String?,
)
