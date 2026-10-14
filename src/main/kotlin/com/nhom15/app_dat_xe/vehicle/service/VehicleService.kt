package com.nhom15.app_dat_xe.vehicle.service

import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
import com.nhom15.app_dat_xe.common.security.AuthUser
import com.nhom15.app_dat_xe.vehicle.dto.CreateVehicleRequest
import com.nhom15.app_dat_xe.vehicle.dto.VehicleResponse
import com.nhom15.app_dat_xe.vehicle.repository.JdbcVehicleRepository
import org.springframework.stereotype.Service

@Service
class VehicleService(private val vehicleRepository: JdbcVehicleRepository) {
    fun list(actor: AuthUser): List<VehicleResponse> {
        requireCustomer(actor)
        return vehicleRepository.findAllByOwnerId(actor.id)
    }

    fun create(actor: AuthUser, request: CreateVehicleRequest): VehicleResponse {
        requireCustomer(actor)
        return vehicleRepository.create(actor.id, request)
    }

    fun delete(actor: AuthUser, id: Long) {
        requireCustomer(actor)
        vehicleRepository.deleteByIdAndOwnerId(id, actor.id)
    }

    private fun requireCustomer(actor: AuthUser) {
        if (actor.role != Role.CUSTOMER) throw ForbiddenException(message = "Chỉ khách hàng được quản lý xe")
    }
}
