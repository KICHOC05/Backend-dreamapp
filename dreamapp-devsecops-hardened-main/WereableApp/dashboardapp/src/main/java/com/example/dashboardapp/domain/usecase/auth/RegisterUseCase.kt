package com.example.dashboardapp.domain.usecase.auth

import com.example.dashboardapp.domain.model.auth.UserInfo
import com.example.dashboardapp.domain.repository.auth.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository,
    private val loginUseCase: LoginUseCase
) {
    suspend operator fun invoke(
        userName: String,
        firstName: String,
        lastName: String,
        password: String,
        roles: List<String>,
        mobilePhone: String,
        phoneOffice: String,
        phoneExt: String,
        email: String,
        active: Boolean,
        role: String
    ): Result<UserInfo> {
        val registerResult = repository.register(
            userName,
            firstName,
            lastName,
            password,
            roles,
            mobilePhone,
            phoneOffice,
            phoneExt,
            email,
            active
        )

        return if (registerResult.isSuccess) {
            loginUseCase(userName, password, role)
        } else {
            Result.failure(registerResult.exceptionOrNull() ?: Exception("Error "))
        }
    }
}