package com.example.appmobile.data.remote.api

import com.example.appmobile.data.remote.dto.SearchUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchUserApiService {
    @GET("/")
    suspend fun searchUser(
        @Query("uidUser") uidUser: String
    ): Response<SearchUserResponse>
}
