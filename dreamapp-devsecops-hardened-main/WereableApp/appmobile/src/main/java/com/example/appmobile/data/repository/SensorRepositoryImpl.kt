package com.example.appmobile.data.repository

import android.content.Context
import android.util.Log
import com.example.appmobile.domain.repository.SensorRepository
import com.example.appmobile.presentation.shared.PhoneDataHolder

class SensorRepositoryImpl(private val context: Context) : SensorRepository {

    override fun saveHeartRate(bpm: Float) {
        Log.i("SensorRepository", "Guardando HR: $bpm")
        PhoneDataHolder.heartRate.value = bpm
        // Aquí puedes agregar lógica para guardar en BD o prefs usando context
    }

    override fun saveHRV(data: String) {
        Log.i("SensorRepository", "Guardando HRV: $data")
        PhoneDataHolder.hrv.value = data
    }

    override fun saveSleepPhase(phase: String) {
        Log.i("SensorRepository", "Guardando SleepPhase: $phase")
        PhoneDataHolder.sleepPhase.value = phase
    }
}