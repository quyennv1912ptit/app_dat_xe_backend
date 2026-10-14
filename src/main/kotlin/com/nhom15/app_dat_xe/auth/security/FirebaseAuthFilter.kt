package com.nhom15.app_dat_xe.auth.security

import com.nhom15.app_dat_xe.account.service.AccountService
import com.nhom15.app_dat_xe.auth.service.FirebaseAuthService
import com.nhom15.app_dat_xe.common.api.ErrorCode
import com.nhom15.app_dat_xe.common.enums.Role
import com.nhom15.app_dat_xe.common.exception.AppException
import com.nhom15.app_dat_xe.common.exception.BadRequestException
import com.nhom15.app_dat_xe.common.exception.NotFoundException
import com.nhom15.app_dat_xe.common.exception.ForbiddenException
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
        return path.startsWith("/api/auth/")
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
        val requestedRole = roleHint(request)
        val adminClaim = token.claims["admin"] == true ||
            token.claims["role"]?.toString()?.equals("ADMIN", ignoreCase = true) == true
        val principal = if (requestedRole == Role.ADMIN || (requestedRole == null && adminClaim)) {
            if (!adminClaim) throw ForbiddenException(message = "Token không có quyền admin")
            val adminId = (token.claims["adminId"] as? Number)?.toLong() ?: 0L
            AuthUser(adminId, Role.ADMIN, token.uid)
        } else {
            val accounts = accountService.findAllByUid(token.uid, requestedRole)
            val account = when {
                accounts.isEmpty() -> throw NotFoundException(ErrorCode.USER_NOT_FOUND)
                accounts.size > 1 ->
                    throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Thiếu role (CUSTOMER hoặc DRIVER)")
                else -> accounts.single()
            }
            AuthUser(account.id, account.role, account.firebaseUid)
        }

        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun roleHint(request: HttpServletRequest): Role? =
        (request.getHeader("X-Role") ?: request.getParameter("role"))?.let { raw ->
            runCatching { Role.valueOf(raw.trim().uppercase()) }.getOrElse {
                throw BadRequestException(ErrorCode.VALIDATION_ERROR, "Role phải là CUSTOMER, DRIVER hoặc ADMIN")
            }
        }

    companion object {
        const val AUTH_ERROR_ATTR = "auth.error"
    }
}
