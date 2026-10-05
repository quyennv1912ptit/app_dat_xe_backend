package com.nhom15.app_dat_xe.auth.security

import com.nhom15.app_dat_xe.auth.service.AccountService
import com.nhom15.app_dat_xe.auth.service.FirebaseAuthService
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.security.AuthUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Đọc "Authorization: Bearer <Firebase ID Token>", verify, rồi đặt [AuthUser] làm principal
 * để controller dùng @CurrentUser. Không phải @Component: SecurityConfig tự tạo để chỉ chạy trong security chain.
 *
 * Role lấy từ header X-Role hoặc query param "role". Nếu không gửi thì suy ra từ DB
 * (chỉ chạy được khi uid chỉ tồn tại ở một trong hai bảng).
 */
class FirebaseAuthFilter(
    private val firebaseAuthService: FirebaseAuthService,
    private val accountService: AccountService
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/auth/") || path.startsWith("/uploads/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith("Bearer ", ignoreCase = true)) {
            try {
                authenticate(request, header.substring(7).trim())
            } catch (e: AppException) {
                SecurityContextHolder.clearContext()
                // Entry point sẽ đọc lại để trả đúng mã lỗi (TOKEN_EXPIRED, TOKEN_INVALID...)
                request.setAttribute(AUTH_ERROR_ATTR, e)
            }
        }
        chain.doFilter(request, response)
    }

    private fun authenticate(request: HttpServletRequest, idToken: String) {
        val token = firebaseAuthService.verifyToken(idToken)
        val accounts = accountService.findAllByUid(token.uid, roleHint(request))

        val account = when {
            accounts.isEmpty() -> throw NotFoundException(ErrorCode.USER_NOT_FOUND)
            accounts.size > 1 ->
                throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Thiếu role (CUSTOMER hoặc DRIVER)")
            else -> accounts.single()
        }

        val authentication = UsernamePasswordAuthenticationToken(
            AuthUser(account.id, account.role),
            null,
            listOf(SimpleGrantedAuthority("ROLE_${account.role.name}"))
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun roleHint(request: HttpServletRequest): Role? =
        (request.getHeader("X-Role") ?: request.getParameter("role"))
            ?.let { runCatching { Role.valueOf(it.trim().uppercase()) }.getOrNull() }

    companion object {
        const val AUTH_ERROR_ATTR = "auth.error"
    }
}
