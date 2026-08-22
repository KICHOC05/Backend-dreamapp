package team.dreamapp.com.presentation.auth

import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.http.UnauthorizedResponse
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.infrastructure.di.RepositoryProvider
import team.dreamapp.com.infrastructure.service.auth.AuditLogger
import team.dreamapp.com.infrastructure.service.auth.AuthTokenService
import team.dreamapp.com.presentation.security.RequestSecurity

object AccessManager {
    private val stateChangingMethods = setOf("POST", "PUT", "PATCH", "DELETE")

    fun handleAccess(ctx: Context) {
        val bearer = ctx.header("Authorization")?.takeIf { it.startsWith("Bearer ", true) }
            ?.substringAfter(' ')?.trim()
        AuthTokenService.resolve(bearer)?.let { ctx.userInfo = it }
        if (ctx.endpoint().path != "/api/image") ctx.refreshUserInfo()
        val permittedRoles = ctx.routeRoles().filterIsInstance<Role>()
        val method = ctx.method().name
        when {
            Role.UNAUTHENTICATED in permittedRoles || permittedRoles.isEmpty() -> {
                if (method in stateChangingMethods) {
                    validateCsrfToken(ctx)
                }
                return
            }
            ctx.userInfo == null -> {
                AuditLogger.log(
                    AuditLogger.Event.UNAUTHORIZED_ACCESS,
                    clientIp = RequestSecurity.clientIp(ctx),
                    detail = "No token provided for $method ${ctx.path()}"
                )
                throw UnauthorizedResponse("Authentication required")
            }
            ctx.userInfo!!.role in permittedRoles -> {
                if (method in stateChangingMethods) {
                    validateCsrfToken(ctx)
                }
                return
            }
            else -> {
                AuditLogger.log(
                    AuditLogger.Event.ROLE_VIOLATION,
                    userId = ctx.userInfo?.id,
                    userName = ctx.userInfo?.userName,
                    clientIp = RequestSecurity.clientIp(ctx),
                    detail = "Role ${ctx.userInfo?.role} not permitted for $method ${ctx.path()}"
                )
                throw UnauthorizedResponse()
            }
        }
    }

    private fun validateCsrfToken(ctx: Context) {
        val origin = ctx.header("Origin") ?: ctx.header("Referer")
        val xRequestedWith = ctx.header("X-Requested-With")
        if (origin == null && xRequestedWith == null) {
            throw ForbiddenResponse("CSRF validation failed: missing Origin/Referer and X-Requested-With headers")
        }
    }

    private fun Context.refreshUserInfo() {
        userInfo?.let {
            val acc = RepositoryProvider.userAccountRepository.userInfoBy("ID", it.id)
            acc?.let { a -> a.role = it.role }
            userInfo = (if (acc != null && acc.active) acc else null) as UserInfo?
        }
    }

    var Context.userInfo: UserInfo?
        get() = this.sessionAttribute("USER_INFO")
        set(userInfo) = this.sessionAttribute("USER_INFO", userInfo)
}
