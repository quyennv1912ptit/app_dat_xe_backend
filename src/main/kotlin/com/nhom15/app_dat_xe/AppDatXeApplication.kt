package com.nhom15.app_dat_xe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class AppDatXeApplication

fun main(args: Array<String>) {
	runApplication<AppDatXeApplication>(*args)
}
