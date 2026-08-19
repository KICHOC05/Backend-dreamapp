package com.example.dashboardapp.domain.repository.sleep

import com.example.dashboardapp.domain.model.sleep.SleepStats

interface SleepRepository {
    suspend fun getAllStatsByUser(uid: String): Result<SleepStats>
}