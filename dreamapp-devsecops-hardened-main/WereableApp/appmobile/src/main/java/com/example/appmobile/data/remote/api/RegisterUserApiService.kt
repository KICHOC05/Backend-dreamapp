package com.example.appmobile.data.remote.api

import com.example.appmobile.data.remote.dto.RegisterUserRequestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RegisterUserApiService {
    @Headers("Content-Type: application/json")
    @POST("/")
    suspend fun registerUser(@Body request: RegisterUserRequestDto): Response<ResponseBody>
}
