package com.example.dashboardapp.domain.usecase.auth

import com.example.dashboardapp.data.remote.dto.auth.LogoutResponseDto
import com.example.dashboardapp.domain.repository.auth.AuthRepository
class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<LogoutResponseDto> {
        val result = repository.logout()
        return if (result.isSuccess && result.getOrNull()?.success == true) {
            repository.deleteLocalUsers()
            Result.success(result.getOrNull()!!)
        } else {
            Result.failure(Exception("Logout failed"))
        }
    }
}