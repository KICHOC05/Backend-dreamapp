package com.example.dashboardapp.domain.usecase.sleep

import com.example.dashboardapp.domain.model.sleep.PredictionEfficiencyNextMonth
import com.example.dashboardapp.domain.repository.sleep.SleepPredictionRepository

class GetPredictEfficiencyNextMonthUseCase(
    private val repository: SleepPredictionRepository
) {
    suspend operator fun invoke(uid: String): Result<PredictionEfficiencyNextMonth?> {
        return repository.getPredictEfficiencyNextMonth(uid)
    }
}