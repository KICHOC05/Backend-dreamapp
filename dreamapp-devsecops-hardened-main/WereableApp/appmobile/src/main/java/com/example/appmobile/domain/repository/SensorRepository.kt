package com.example.appmobile.domain.repository

interface SensorRepository {
    fun saveHeartRate(bpm: Float)
    fun saveHRV(data: String)
    fun saveSleepPhase(phase: String)
}