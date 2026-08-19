package com.example.dashboardapp.domain.repository.sleep

import com.example.dashboardapp.domain.model.sleep.PredictionEfficiencyNextMonth

interface SleepPredictionRepository {
    suspend fun getPredictEfficiencyNextMonth(uid: String): Result<PredictionEfficiencyNextMonth?>
}