package com.example.dashboardapp.data.remote.dto.auth

data class LoginRequestDto(
    val userName: String,
    val password: String,
    val role: String
)