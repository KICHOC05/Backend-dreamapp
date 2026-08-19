package com.example.appmobile.domain.repository

interface WearConnectionRepository {
    suspend fun sendHeartRate(bpm: Float)
    suspend fun sendHRV(rmssd: Double, sdnn: Double)
    suspend fun sendSleepPhase(phase: String)
    suspend fun sendUserData(
        edad: Int,
        peso: Float,
        estatura: Float,
        sexo: String
    )
}
