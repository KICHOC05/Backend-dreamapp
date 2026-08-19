package com.example.wereableapp.presentation.data.repository

import com.example.wereableapp.presentation.data.local.entity.SleepPhaseDataEntity

interface SleepPhaseRepository {
    suspend fun insert(record: SleepPhaseDataEntity)
    suspend fun getLast(): SleepPhaseDataEntity?
}
