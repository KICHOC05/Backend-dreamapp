package com.example.dashboardapp.data.remote.dto.auth

data class LoginResponseDto(
    val success: Boolean,
    val data: UserInfoDto?
)