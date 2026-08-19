package com.example.dashboardapp.data.remote.dto.users

import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.domain.model.user.Sex
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName(value = "id", alternate = ["uidUser"]) val id: String,
    @SerializedName(value = "name", alternate = ["username"]) val name: String,
    @SerializedName(value = "weight", alternate = ["weightKg"]) val weight: Int,
    @SerializedName(value = "height", alternate = ["heightCm"]) val height: Int,
    val age: Int,
    val sex: String,
    @SerializedName(value = "pictureUrl", alternate = ["profilePictureUrl"]) val pictureUrl: String
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        weight = weight,
        height = height,
        age = age,
        sex = Sex.fromString(sex),
        pictureUrl = pictureUrl
    )
}