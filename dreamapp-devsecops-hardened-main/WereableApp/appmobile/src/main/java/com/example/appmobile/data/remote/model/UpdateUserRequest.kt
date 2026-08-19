package com.example.appmobile.data.remote.model

data class UpdateUserRequest(
    val weightKg: Double?,
    val heightCm: Double?,
    val age: Int?,
    val sex: String?
)