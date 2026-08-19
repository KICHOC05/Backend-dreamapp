package team.dreamapp.com.presentation.auth

import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.infrastructure.di.RepositoryProvider
import team.dreamapp.com.infrastructure.service.auth.AuthTokenService

object AccessManager {
    // Handler End points according to permitted roles
    fun handleAccess(ctx: Context) {
        val bearer = ctx.header("Authorization")?.takeIf { it.startsWith("Bearer ", true) }
            ?.substringAfter(' ')?.trim()
        AuthTokenService.resolve(bearer)?.let { ctx.userInfo = it }
        if (ctx.matchedPath() != "/api/image") ctx.refreshUserInfo()
        val permittedRoles = ctx.routeRoles()
        when {
            Role.UNAUTHENTICATED in permittedRoles || permittedRoles.isEmpty() -> return
            ctx.userInfo == null -> throw UnauthorizedResponse("Authentication required")
            ctx.userInfo!!.role in permittedRoles -> return
            else -> throw UnauthorizedResponse()
        }
    }

    // Context Extension function to refresh User Info
    private fun Context.refreshUserInfo() {
        userInfo?.let {
            val acc = RepositoryProvider.userAccountRepository.userInfoBy("ID", it.id)
            acc?.let { a -> a.role = it.role }
            userInfo = (if (acc != null && acc.active) acc else null) as UserInfo?
        }
    }

    // Context Extension function to GET/SET userInfo
    var Context.userInfo: UserInfo?
        get() = this.sessionAttribute("USER_INFO")
        set(userInfo) = this.sessionAttribute("USER_INFO", userInfo)
}
