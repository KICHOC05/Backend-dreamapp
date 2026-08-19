package team.dreamapp.com.domain.usecase.auth

import team.dreamapp.com.domain.services.auth.AuthService
import team.dreamapp.com.domain.entity.auth.UserInfo
import io.javalin.http.Context

class LoginUseCase(private val authService: AuthService) {
    fun execute(userName: String, password: String, role: String, ctx: Context): UserInfo {
        val userInfo = authService.login(userName, password, role)
        ctx.sessionAttribute("USER_INFO", userInfo)
        return userInfo
    }
}
