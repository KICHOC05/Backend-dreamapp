package com.example.appmobile.data.mapper

import com.example.appmobile.data.database.entity.UserDataEntity
import com.example.appmobile.data.remote.model.UserDataRequest

fun UserDataEntity.toRequest(uid: String) = UserDataRequest(
    uidUser = id.toString(),
    weightKg = peso.toFloat(),
    heightCm = estatura.toFloat(),
    age = edad,
    sex = sexo
)
