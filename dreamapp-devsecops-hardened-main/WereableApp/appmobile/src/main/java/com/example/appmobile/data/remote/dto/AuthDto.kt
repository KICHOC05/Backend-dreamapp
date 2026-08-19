package com.example.appmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchUserResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Boolean
)

data class RegisterUserRequestDto(
    @SerializedName("uidUser") val uidUser: String,
    @SerializedName("weightKg") val weightKg: Double,
    @SerializedName("heightCm") val heightCm: Double,
    @SerializedName("age") val age: Int,
    @SerializedName("sex") val sex: String
)

data class RegisterUserResponseDto(
    @SerializedName("message") val message: String
)
