package com.example.wereableapp.presentation.domain.repository

interface WearConnectionRepository {
    suspend fun sendHeartRate(bpm: Float)
    suspend fun sendHRV(rmssd: Double, sdnn: Double)
    suspend fun sendSleepPhase(phase: String)
    suspend fun sendSleepJson(json: String)
}