package com.example.dashboardapp.data.repository.auth

import com.example.dashboardapp.data.remote.api.auth.AuthApiService
import com.example.dashboardapp.data.remote.dto.auth.LoginRequestDto
import com.example.dashboardapp.data.remote.dto.auth.RegisterRequestDto
import com.example.dashboardapp.data.remote.dto.auth.toDomain
import com.example.dashboardapp.data.remote.dto.auth.UserInfoDto
import com.example.dashboardapp.data.local.dao.UserDao
import com.example.dashboardapp.data.local.mapper.toEntity
import com.example.dashboardapp.domain.model.auth.UserInfo
import com.example.dashboardapp.domain.repository.auth.AuthRepository
import com.example.dashboardapp.data.remote.dto.auth.LogoutResponseDto
import com.example.dashboardapp.data.remote.dto.auth.RegisterResponseDto

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val userDao: UserDao
) : AuthRepository {
    override suspend fun login(userName: String, password: String, role: String): Result<UserInfo> {
        val response = api.login(LoginRequestDto(userName, password, role))
        return if (response.isSuccessful && response.body()?.success == true) {
            val userInfoDto: UserInfoDto = response.body()!!.data!!
            userDao.insertUser(userInfoDto.toEntity())
            Result.success(userInfoDto.toDomain())
        } else {
            Result.failure(Exception("Login failed"))
        }
    }

    override suspend fun logout(): Result<LogoutResponseDto> {
        val response = api.logout()
        return if (response.isSuccessful && response.body()?.success == true) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Logout failed"))
        }
    }

    override suspend fun deleteLocalUsers() {
        userDao.deleteAllUsers()
    }

    override suspend fun register(userName: String, firstName: String, lastName: String, password: String, roles: List<String>, mobilePhone: String, phoneOffice: String, phoneExt: String, email: String, active: Boolean): Result<RegisterResponseDto> {
        val response = api.register(RegisterRequestDto(userName, firstName, lastName, password, roles, mobilePhone, phoneOffice, phoneExt, email, active))
        return if (response.isSuccessful && response.body()?.success == true) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Register failed"))
        }
    }
}