package com.example.dashboardapp.data.remote.dto.auth

data class RegisterRequestDto(
    val userName: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val roles: List<String>,
    val mobilePhone: String,
    val phoneOffice: String,
    val phoneExt: String,
    val email: String,
    val active: Boolean
)