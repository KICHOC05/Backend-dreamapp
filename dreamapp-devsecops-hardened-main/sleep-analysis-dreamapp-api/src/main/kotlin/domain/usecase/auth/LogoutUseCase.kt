package team.dreamapp.com.domain.usecase.auth

import io.javalin.http.Context
import team.dreamapp.com.domain.services.auth.AuthService

class LogoutUseCase(private val authService: AuthService) {
    fun execute(ctx: Context): Boolean {
        return authService.logout(ctx)
    }
}