package com.example.dashboardapp.data.remote.api.sleep

import com.example.dashboardapp.data.remote.dto.sleep.StatsByUserResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SleepApiService {
    @GET("/sleep/stats")
    suspend fun getAllStatsByUser(
        @Query("uid") uid: String
    ): Response<StatsByUserResponseDto>
}