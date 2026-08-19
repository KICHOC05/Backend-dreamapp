package com.example.dashboardapp.data.repository.sleep

import com.example.dashboardapp.data.remote.api.sleep.SleepPredictionApiService
import com.example.dashboardapp.data.remote.dto.sleep.toDomain
import com.example.dashboardapp.domain.model.sleep.PredictionEfficiencyNextMonth
import com.example.dashboardapp.domain.repository.sleep.SleepPredictionRepository
import javax.inject.Inject

class SleepPredictionRepositoryImpl @Inject constructor(
    private val api: SleepPredictionApiService
) : SleepPredictionRepository {
    override suspend fun getPredictEfficiencyNextMonth(uid: String): Result<PredictionEfficiencyNextMonth?> {
        return try {
            val response = api.getPredictEfficiencyNextMonth(uid)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val domainData = body.toDomain()
                    if (domainData != null) {
                        Result.success(domainData)
                    } else {
                        Result.failure(Exception("Datos vacíos recibidos - data field is null"))
                    }
                } else {
                    Result.failure(Exception("Response empty or error in server - success: ${body?.success}"))
                }
            } else {
                Result.failure(Exception("Error to get prediction efficiency: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}