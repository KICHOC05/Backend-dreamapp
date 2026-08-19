package com.example.dashboardapp.domain.repository.auth

import com.example.dashboardapp.data.remote.dto.auth.LogoutResponseDto
import com.example.dashboardapp.data.remote.dto.auth.RegisterResponseDto
import com.example.dashboardapp.domain.model.auth.UserInfo

interface AuthRepository {
    suspend fun login(userName: String, password: String, role: String): Result<UserInfo>
    suspend fun logout(): Result<LogoutResponseDto>
    suspend fun deleteLocalUsers()
    suspend fun register(userName: String, firstName: String, lastName: String, password: String, roles: List<String>, mobilePhone: String, phoneOffice: String, phoneExt: String, email: String, active: Boolean): Result<RegisterResponseDto>
}