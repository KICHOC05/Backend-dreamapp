package team.dreamapp.com.domain.usecase.auth

import team.dreamapp.com.domain.services.auth.AuthService
import team.dreamapp.com.domain.entity.auth.UserInfo

class LoginUseCase(private val authService: AuthService) {
    fun execute(userName: String, password: String, role: String): UserInfo =
        authService.login(userName, password, role)
}
