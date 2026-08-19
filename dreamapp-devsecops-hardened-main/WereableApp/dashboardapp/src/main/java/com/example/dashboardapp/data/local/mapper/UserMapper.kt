package com.example.dashboardapp.data.local.mapper

import com.example.dashboardapp.data.local.entity.UserEntity
import com.example.dashboardapp.data.remote.dto.auth.UserInfoDto

fun UserInfoDto.toEntity(): UserEntity = UserEntity(
    id = id,
    userName = userName,
    fullname = fullname,
    role = role,
    photoUrl = photoUrl,
    active = active,
    email = null
)
