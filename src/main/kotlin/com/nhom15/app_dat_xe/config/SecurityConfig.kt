package com.nhom15.app_dat_xe.config

import com.nhom15.app_dat_xe.auth.security.ApiAccessDeniedHandler
import com.nhom15.app_dat_xe.auth.security.ApiAuthenticationEntryPoint
import com.nhom15.app_dat_xe.auth.security.FirebaseAuthFilter
import com.nhom15.app_dat_xe.account.service.AccountService
import com.nhom15.app_dat_xe.auth.service.FirebaseAuthService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        firebaseAuthService: FirebaseAuthService,
        accountService: AccountService,
        entryPoint: ApiAuthenticationEntryPoint,
        accessDeniedHandler: ApiAccessDeniedHandler
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth/**",
                        "/uploads/avatars/**",
                        "/error",
                        "/ws-location/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                FirebaseAuthFilter(firebaseAuthService, accountService),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}