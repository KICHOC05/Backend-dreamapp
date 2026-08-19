package com.example.appmobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class GetUserByUidResponse(
    @SerializedName("id") val id: String,
    @SerializedName("data") val data: UserDataFromApi
)

data class UserDataFromApi(
    @SerializedName("uidUser") val uidUser: String,
    @SerializedName("weightKg") val weightKg: Double,
    @SerializedName("heightCm") val heightCm: Double,
    @SerializedName("age") val age: Int,
    @SerializedName("sex") val sex: String
)
