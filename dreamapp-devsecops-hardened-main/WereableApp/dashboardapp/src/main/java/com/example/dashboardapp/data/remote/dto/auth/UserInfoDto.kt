package com.example.dashboardapp.data.remote.dto.auth

import com.example.dashboardapp.domain.model.auth.Role
import com.example.dashboardapp.domain.model.auth.UserInfo

data class UserInfoDto(
    val id: String,
    val userName: String,
    val password: String,
    val fullname: String,
    val role: String,
    val roles: List<String>,
    val active: Boolean,
    val currentDate: String,
    val photoUrl: String
)

fun UserInfoDto.toDomain(): UserInfo =
    UserInfo(
        id = id,
        userName = userName,
        role = when (role.uppercase()) {
            "SYSADMIN" -> Role.SYSADMIN
            "ADMIN" -> Role.ADMIN
            "CLIENTE" -> Role.CLIENT
            else -> Role.UNAUTHENTICATED
        }
    )