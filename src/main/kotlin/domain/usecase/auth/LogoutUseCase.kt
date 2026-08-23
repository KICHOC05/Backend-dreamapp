package team.dreamapp.com.domain.usecase.auth

import team.dreamapp.com.domain.services.auth.AuthService

class LogoutUseCase(private val authService: AuthService) {
    fun execute(): Boolean = authService.logout()
}
