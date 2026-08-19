package com.example.appmobile.data.remote.api

import com.example.appmobile.data.remote.dto.RegisterUserRequestDto
import com.example.appmobile.data.remote.dto.RegisterUserResponseDto
import com.example.appmobile.data.remote.dto.SearchUserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @GET("/searchUser")
    suspend fun searchUser(@Query("uidUser") uidUser: String): Response<SearchUserResponseDto>
    
    @POST("/registerUser")
    suspend fun registerUser(@Body request: RegisterUserRequestDto): Response<RegisterUserResponseDto>
}
