package com.example.dashboardapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: String,
    val userName: String,
    val fullname: String?,
    val role: String?,
    val photoUrl: String?,
    val active: Boolean,
    val email: String?
)
