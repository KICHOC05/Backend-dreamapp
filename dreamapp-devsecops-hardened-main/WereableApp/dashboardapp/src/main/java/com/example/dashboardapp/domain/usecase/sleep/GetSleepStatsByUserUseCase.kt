package com.example.dashboardapp.domain.usecase.sleep

import com.example.dashboardapp.domain.model.sleep.SleepStats
import com.example.dashboardapp.domain.repository.sleep.SleepRepository

class GetSleepStatsByUserUseCase(
    private val repository: SleepRepository
) {
    suspend operator fun invoke(uid: String): Result<SleepStats> {
        return repository.getAllStatsByUser(uid)
    }
}