package com.example.dashboardapp.data.repository.sleep

import com.example.dashboardapp.data.remote.api.sleep.SleepApiService
import com.example.dashboardapp.data.remote.dto.sleep.toDomain
import com.example.dashboardapp.domain.model.sleep.SleepStats
import com.example.dashboardapp.domain.repository.sleep.SleepRepository
import javax.inject.Inject

class SleepRepositoryImpl @Inject constructor(
    private val api: SleepApiService
) : SleepRepository {

    override suspend fun getAllStatsByUser(uid: String): Result<SleepStats> {
        return try {
            val response = api.getAllStatsByUser(uid)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.toDomain())
                } else {
                    Result.failure(Exception("Response empty or error in server"))
                }
            } else {
                Result.failure(Exception("Error to get stats: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}