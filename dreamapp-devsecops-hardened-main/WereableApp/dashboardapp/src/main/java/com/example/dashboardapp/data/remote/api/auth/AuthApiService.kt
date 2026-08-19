package com.example.dashboardapp.data.remote.api.auth

import com.example.dashboardapp.data.remote.dto.auth.LoginRequestDto
import com.example.dashboardapp.data.remote.dto.auth.LoginResponseDto
import com.example.dashboardapp.data.remote.dto.auth.LogoutResponseDto
import com.example.dashboardapp.data.remote.dto.auth.RegisterRequestDto
import com.example.dashboardapp.data.remote.dto.auth.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("/account")
    suspend fun register(@Body request: RegisterRequestDto): Response<RegisterResponseDto>

    @POST("/auth/logout")
    suspend fun logout(): Response<LogoutResponseDto>
}