package com.example.appmobile.data.remote.api

import com.example.appmobile.data.remote.model.SearchUserResponse
import com.example.appmobile.data.remote.model.UpdateUserRequest
import com.example.appmobile.data.remote.model.UserDataRequest
import com.example.appmobile.data.remote.model.UserDataResponse
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

interface UserApi {
    @POST("registerUser")
    suspend fun registerUser(@Body userData: UserDataRequest): Response<String>

    @GET("getUserByUid")
    suspend fun getUserByUid(@Query("uidUser") uid: String): Response<UserDataResponse>

    @GET("searchUser")
    suspend fun searchUser(@Query("uidUser") uid: String): Response<SearchUserResponse>

    @PUT("updateUser")
    suspend fun updateUser(
        @Query("uidUser") uid: String,
        @Body userData: UpdateUserRequest
    ): Response<String>
}