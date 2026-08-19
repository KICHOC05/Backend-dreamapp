package com.example.appmobile.data.mapper

import com.example.appmobile.data.remote.model.UserData
import com.example.appmobile.domain.model.UserDataModel

fun UserData.toDomainModel(): UserDataModel {
    return UserDataModel(
        uidUser = this.uidUser,
        weightKg = this.weightKg.toFloat(),
        heightCm = this.heightCm.toFloat(),
        age = this.age,
        sex = this.sex
    )
}
