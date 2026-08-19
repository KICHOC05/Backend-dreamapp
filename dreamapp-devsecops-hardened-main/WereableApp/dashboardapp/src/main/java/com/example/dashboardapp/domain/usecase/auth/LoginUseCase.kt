package com.example.dashboardapp.domain.usecase.auth

import com.example.dashboardapp.domain.model.auth.UserInfo
import com.example.dashboardapp.domain.repository.auth.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userName: String, password: String, role: String): Result<UserInfo> {
        return repository.login(userName, password, role)
    }
}