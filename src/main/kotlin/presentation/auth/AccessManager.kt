package team.dreamapp.com.presentation.auth

import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import io.javalin.http.UnauthorizedResponse
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.infrastructure.di.RepositoryProvider
import team.dreamapp.com.infrastructure.service.auth.AuditLogger
import team.dreamapp.com.infrastructure.service.auth.AuthSessionCookie
import team.dreamapp.com.infrastructure.service.auth.AuthTokenService
import team.dreamapp.com.presentation.security.RequestSecurity
import team.dreamapp.com.presentation.security.WebOriginPolicy

object AccessManager {
    private val stateChangingMethods = setOf("POST", "PUT", "PATCH", "DELETE")
    private const val userInfoAttribute = "AUTHENTICATED_USER_INFO"
    private const val webRequestHeader = "X-DreamApp-Request"
    private const val webRequestValue = "DreamAppWeb"

    fun handleAccess(ctx: Context) {
        val bearer = ctx.header("Authorization")?.takeIf { it.startsWith("Bearer ", true) }
            ?.substringAfter(' ')?.trim()
        val cookieToken = AuthSessionCookie.read(ctx)
        val token = bearer ?: cookieToken
        AuthTokenService.resolve(token)?.let { ctx.userInfo = it }
        if (cookieToken != null && ctx.userInfo == null) AuthSessionCookie.clear(ctx)
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
        val bearerRequest = ctx.header("Authorization")?.startsWith("Bearer ", true) == true
        if (bearerRequest) return

        val originOrReferer = ctx.header("Origin") ?: ctx.header("Referer")
        val browserRequest = originOrReferer != null || ctx.header("Sec-Fetch-Site") != null
        val cookieRequest = AuthSessionCookie.read(ctx) != null
        if (!browserRequest && !cookieRequest) return

        val validOrigin = WebOriginPolicy.isAllowed(originOrReferer)
        val validHeader = ctx.header(webRequestHeader) == webRequestValue
        if (!validOrigin || !validHeader) {
            throw ForbiddenResponse("CSRF validation failed")
        }
    }

    private fun Context.refreshUserInfo() {
        userInfo?.let {
            val acc = RepositoryProvider.userAccountRepository.userInfoBy("ID", it.id)
            acc?.let { a -> a.role = it.role }
            userInfo = (if (acc != null && acc.active) acc else null) as UserInfo?
        }
    }

    fun currentUser(ctx: Context): UserInfo? = ctx.attribute(userInfoAttribute)

    var Context.userInfo: UserInfo?
        get() = attribute(userInfoAttribute)
        set(userInfo) = attribute(userInfoAttribute, userInfo)
}
