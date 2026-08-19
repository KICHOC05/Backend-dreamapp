package com.example.dashboardapp.data.remote.api.user

import com.example.dashboardapp.data.remote.dto.users.UserDto
import retrofit2.Response
import retrofit2.http.GET

interface UserApiService {
    @GET("/users")
    suspend fun getAllUsers(): Response<List<UserDto>>
}