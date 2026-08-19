package com.example.dashboardapp.data.remote.api.sleep

import com.example.dashboardapp.data.remote.dto.sleep.PredictionEfficiencyNextMonthResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SleepPredictionApiService {
    @GET("/ai/predictions-next-month-efficiency")
    suspend fun getPredictEfficiencyNextMonth(
        @Query("uid") uid: String
    ): Response<PredictionEfficiencyNextMonthResponseDto>
}