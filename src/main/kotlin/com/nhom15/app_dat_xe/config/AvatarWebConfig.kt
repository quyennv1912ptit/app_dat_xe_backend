package com.nhom15.app_dat_xe.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
class AvatarWebConfig(
    @Value("\${app.upload.avatar-dir:uploads/avatars}")
    private val uploadDir: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val directory = Path.of(uploadDir)
            .toAbsolutePath()
            .normalize()
            .toUri()
            .toString()

        val resourceLocation =
            if (directory.endsWith("/")) directory else "$directory/"

        registry.addResourceHandler("/uploads/avatars/**")
            .addResourceLocations(resourceLocation)
    }
}