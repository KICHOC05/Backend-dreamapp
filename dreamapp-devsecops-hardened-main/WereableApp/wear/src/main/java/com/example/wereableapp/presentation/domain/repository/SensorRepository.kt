package com.example.wereableapp.presentation.domain.repository

/*
* Implementacion de la interfaz SensorRepository.
* */

import com.example.wereableapp.presentation.domain.model.Accelerometer
import com.example.wereableapp.presentation.domain.model.HeartRate
import com.example.wereableapp.presentation.domain.model.HeartRateV
import com.example.wereableapp.presentation.domain.model.SleepPhase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
    fun heartRateFlow(): StateFlow<HeartRate?>
    fun accelerometerFlow(): StateFlow<Accelerometer?>
    fun hrvFlow(): Flow<HeartRateV?>
    fun sleepPhaseFlow(): Flow<SleepPhase>
    fun startSensors()
    fun stopSensors()
}
