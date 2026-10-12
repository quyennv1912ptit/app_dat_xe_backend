package com.nhom15.app_dat_xe.config

import com.nhom15.app_dat_xe.common.port.CustomerVehicleQueryPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Giữ ride-core khởi động khi không có adapter dữ liệu xe. */
@Configuration
class RideIntegrationConfig {
    @Bean
    @ConditionalOnMissingBean(CustomerVehicleQueryPort::class)
    fun unavailableCustomerVehicleQueryPort(): CustomerVehicleQueryPort =
        object : CustomerVehicleQueryPort {
            override fun findOwnedVehicle(vehicleId: Long, customerId: Long) = null
        }
}
