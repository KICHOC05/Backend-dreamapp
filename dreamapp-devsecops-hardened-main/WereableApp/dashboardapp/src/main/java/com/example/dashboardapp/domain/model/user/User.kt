package com.example.dashboardapp.domain.model.user

data class User(
    val id: String,
    val name: String,
    val weight: Int,
    val height: Int,
    val age: Int,
    val sex: Sex,
    val pictureUrl: String
)