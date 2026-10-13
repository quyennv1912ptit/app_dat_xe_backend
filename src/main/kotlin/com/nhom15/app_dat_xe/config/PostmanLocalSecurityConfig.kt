package com.nhom15.app_dat_xe.config

import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.security.AuthUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

/** No-token access to one read-only API, explicitly enabled for loopback-only local testing. */
@Configuration(proxyBeanMethods = false)
@Profile("postman-local")
class PostmanLocalSecurityConfig(
    @Value("\${server.address:}") serverAddress: String,
) {
    init {
        require(serverAddress == "127.0.0.1" || serverAddress == "::1") {
            "postman-local requires server.address=127.0.0.1 or ::1"
        }
    }

    @Bean
    @Order(0)
    fun postmanPricingListSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher { request ->
                request.method == "GET" && request.servletPath == "/admin/pricing-rules"
            }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .requestCache { it.disable() }
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .addFilterBefore(LocalPricingReaderFilter(), AnonymousAuthenticationFilter::class.java)
        return http.build()
    }
}

// Constructed inside the selected chain, not a servlet filter bean: it cannot affect other APIs.
private class LocalPricingReaderFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.remoteAddr !in LOOPBACK_ADDRESSES) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        val originalContext = SecurityContextHolder.getContext()
        val localContext = SecurityContextHolder.createEmptyContext()
        // A test principal only; this endpoint does not write records or audit actor IDs.
        localContext.authentication = UsernamePasswordAuthenticationToken(
            AuthUser(id = 0L, role = Role.ADMIN),
            null,
            listOf(SimpleGrantedAuthority("ROLE_ADMIN")),
        )
        SecurityContextHolder.setContext(localContext)
        try {
            filterChain.doFilter(request, response)
        } finally {
            SecurityContextHolder.setContext(originalContext)
        }
    }

    companion object {
        val LOOPBACK_ADDRESSES = setOf("127.0.0.1", "::1", "0:0:0:0:0:0:0:1")
    }
}
