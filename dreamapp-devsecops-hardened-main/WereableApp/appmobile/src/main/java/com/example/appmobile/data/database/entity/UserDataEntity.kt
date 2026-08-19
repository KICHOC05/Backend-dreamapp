package com.example.appmobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val edad: Int,
    val peso: Float,
    val estatura: Float,
    val sexo: String
)
/*
//Colocar solo en caso de Error jsjsjs
import com.example.appmobile.data.database.entity.UserDataEntity
import com.example.appmobile.data.network.model.UserDataRequest

fun UserDataEntity.toRequest(uid: String) = UserDataRequest(
    uidUser = uid,
    weightKg = peso.toDouble(),
    heightCm = estatura.toDouble(),
    age = edad,
    sex = sexo
)
 */