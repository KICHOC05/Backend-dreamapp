package com.example.dashboardapp.domain.model.auth

data class UserInfo(
    val id: String,
    val userName: String,
    val role: Role
)