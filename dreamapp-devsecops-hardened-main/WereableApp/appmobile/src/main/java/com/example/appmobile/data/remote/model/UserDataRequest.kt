package com.example.appmobile.data.remote.model

data class UserDataRequest(
    val uidUser: String,
    val weightKg: Float,
    val heightCm: Float,
    val age: Int,
    val sex: String
)