package com.example.wereableapp.presentation.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wereableapp.presentation.data.local.entity.SleepPhaseDataEntity
import com.example.wereableapp.presentation.data.repository.SleepPhaseRepository
import com.example.wereableapp.presentation.domain.model.Accelerometer
import com.example.wereableapp.presentation.domain.model.HeartRate
import com.example.wereableapp.presentation.domain.model.HeartRateV
import com.example.wereableapp.presentation.domain.model.SleepPhase
import com.example.wereableapp.presentation.domain.repository.SensorRepository
import com.example.wereableapp.presentation.domain.repository.WearConnectionRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SleepMonitorViewModel(
    private val repository: SensorRepository,
    private val wearConnectionRepository: WearConnectionRepository,
    private val sleepPhaseRepository: SleepPhaseRepository // 👈 Agrega este
) : ViewModel() {

    val heartRate: StateFlow<HeartRate?> = repository.heartRateFlow()
    val accelerometer: StateFlow<Accelerometer?> = repository.accelerometerFlow()

    val hrv: StateFlow<HeartRateV?> = repository
        .hrvFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    val sleepPhase: StateFlow<SleepPhase> = repository
        .sleepPhaseFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SleepPhase.AWAKE
        )

    init {
        observeAndSendData()
        observeAndSaveSleepPhase() // 👈 Agrega esta línea
    }

    fun startMonitoring() {
        repository.startSensors()
    }

    fun stopMonitoring() {
        repository.stopSensors()
    }

    fun sendTestData() {
        viewModelScope.launch {
            Log.i("WearableVM", "🧪 INICIANDO ENVÍO DE DATOS DE PRUEBA")
            try {
                // Enviar Heart Rate
                Log.i("WearableVM", "📤 Enviando HR test: 75.0")
                wearConnectionRepository.sendHeartRate(75.0f)
                kotlinx.coroutines.delay(500) // Pequeño delay entre envíos
                
                // Enviar HRV
                Log.i("WearableVM", "📤 Enviando HRV test: 45.2, 50.1")
                wearConnectionRepository.sendHRV(45.2, 50.1)
                kotlinx.coroutines.delay(500)
                
                // Enviar Sleep Phase
                Log.i("WearableVM", "📤 Enviando Phase test: DEEP")
                wearConnectionRepository.sendSleepPhase("DEEP")
                
                Log.i("WearableVM", "✅ TODOS LOS DATOS DE PRUEBA ENVIADOS")
            } catch (e: Exception) {
                Log.e("WearableVM", "❌ Error enviando datos de prueba: ${e.message}")
            }
        }
    }

    private fun observeAndSendData() {
        viewModelScope.launch {
            heartRate.collect { hr ->
                hr?.let {
                    wearConnectionRepository.sendHeartRate(it.bpm)
                }
            }
        }

        viewModelScope.launch {
            hrv.collect { hrvData ->
                hrvData?.let {
                    wearConnectionRepository.sendHRV(it.rmssd, it.sdnn)
                }
            }
        }

        viewModelScope.launch {
            sleepPhase.collect { phase ->
                wearConnectionRepository.sendSleepPhase(phase.name)
            }
        }
    }
    private var lastRecord: SleepPhaseDataEntity? = null
    private var lastInsertTime: Long = 0L
    private val insertIntervalMillis = 10_000L // 10 segundos
    private val bpmThreshold = 5f // diferencia mínima de BPM para considerar cambio

    private fun observeAndSaveSleepPhase() {
        viewModelScope.launch {
            combine(
                heartRate,
                hrv,
                accelerometer,
                sleepPhase
            ) { hr, hrvData, accel, phase ->
                if (hr != null && hrvData != null && accel != null) {
                    SleepPhaseDataEntity(
                        timestamp = System.currentTimeMillis(),
                        phase = phase.name,
                        bpm = hr.bpm,
                        rmssd = hrvData.rmssd,
                        sdnn = hrvData.sdnn,
                        movement = (accel.x * accel.x + accel.y * accel.y + accel.z * accel.z).toDouble()
                    )
                } else null
            }.collect { record ->
                record?.let {
                    val currentTime = System.currentTimeMillis()
                    val timeElapsed = currentTime - lastInsertTime

                    val bpmChanged = lastRecord?.let { prev -> kotlin.math.abs(prev.bpm - it.bpm) >= bpmThreshold } ?: true
                    val phaseChanged = lastRecord?.phase != it.phase
                    val isDifferentEnough = bpmChanged || phaseChanged
                    val isTimePassed = timeElapsed >= insertIntervalMillis

                    if (isTimePassed || isDifferentEnough) {
                        sleepPhaseRepository.insert(it)
                        lastRecord = it
                        lastInsertTime = currentTime
                        Log.d("SleepViewModel", "✅ Registro insertado: $it")
                    } else {
                        Log.d(
                            "SleepViewModel",
                            "⏳ Registro omitido - Tiempo: $timeElapsed ms, Diferencia suficiente: $isDifferentEnough"
                        )
                    }
                }
            }
        }
    }
    // En SleepMonitorViewModel.kt
    fun sendFakeSleepCycle() {
        val json = """
        {
          "uidUser": "l20rMb0Sz6QjViBxPdi5Yfab8R23",
          "deviceId": "smartwatch001",
          "date": "2025-08-11",
          "startTime": "2025-08-10T22:30:00.000Z",
          "endTime": "2025-08-11T06:45:00.000Z",
          "timezone": "America/Mexico_City",
          "totalDuration": 480,
          "sleepDuration": 420,
          "lightSleepMinutes": 200,
          "deepSleepMinutes": 120,
          "remSleepMinutes": 100,
          "awakeDuration": 60,
          "sleepEfficiency": 87.5,
          "awakeningsCount": 3,
          "quality": "GOOD",
          "avgHeartRate": 62,
          "minHeartRate": 48,
          "maxHeartRate": 78,
          "avgMovement": 15,
          "avgRmssd": 45.2,
          "avgSdnn": 52.1,
          "sleepPhaseData": [
            { "id": 1, "phase": "AWAKE", "datetime": "2025-08-10T22:30:00.000Z", "hr_bpm": 74, "hrv_rmssd": 30.2, "hrv_sdnn": 36.5 },
            { "id": 2, "phase": "AWAKE", "datetime": "2025-08-10T22:35:00.000Z", "hr_bpm": 72, "hrv_rmssd": 31.0, "hrv_sdnn": 37.2 },
        
            { "id": 3, "phase": "LIGHT", "datetime": "2025-08-10T22:35:00.000Z", "hr_bpm": 68, "hrv_rmssd": 38.5, "hrv_sdnn": 44.1 },
            { "id": 4, "phase": "LIGHT", "datetime": "2025-08-10T22:55:00.000Z", "hr_bpm": 66, "hrv_rmssd": 40.1, "hrv_sdnn": 46.0 },
        
            { "id": 5, "phase": "DEEP", "datetime": "2025-08-10T22:55:00.000Z", "hr_bpm": 54, "hrv_rmssd": 55.8, "hrv_sdnn": 63.4 },
            { "id": 6, "phase": "DEEP", "datetime": "2025-08-10T23:40:00.000Z", "hr_bpm": 52, "hrv_rmssd": 58.2, "hrv_sdnn": 66.1 },
        
            { "id": 7, "phase": "REM", "datetime": "2025-08-10T23:40:00.000Z", "hr_bpm": 70, "hrv_rmssd": 42.3, "hrv_sdnn": 48.2 },
            { "id": 8, "phase": "REM", "datetime": "2025-08-11T00:10:00.000Z", "hr_bpm": 68, "hrv_rmssd": 40.9, "hrv_sdnn": 47.0 },
        
            { "id": 9, "phase": "AWAKE", "datetime": "2025-08-11T00:10:00.000Z", "hr_bpm": 75, "hrv_rmssd": 32.1, "hrv_sdnn": 39.0 },
            { "id": 10, "phase": "AWAKE", "datetime": "2025-08-11T00:12:00.000Z", "hr_bpm": 73, "hrv_rmssd": 31.4, "hrv_sdnn": 38.2 },
        
            { "id": 11, "phase": "LIGHT", "datetime": "2025-08-11T00:12:00.000Z", "hr_bpm": 64, "hrv_rmssd": 41.7, "hrv_sdnn": 49.2 },
            { "id": 12, "phase": "LIGHT", "datetime": "2025-08-11T00:35:00.000Z", "hr_bpm": 63, "hrv_rmssd": 43.0, "hrv_sdnn": 50.1 },
        
            { "id": 13, "phase": "DEEP", "datetime": "2025-08-11T00:35:00.000Z", "hr_bpm": 51, "hrv_rmssd": 57.9, "hrv_sdnn": 64.0 },
            { "id": 14, "phase": "DEEP", "datetime": "2025-08-11T01:05:00.000Z", "hr_bpm": 50, "hrv_rmssd": 59.1, "hrv_sdnn": 65.3 },
        
            { "id": 15, "phase": "REM", "datetime": "2025-08-11T01:05:00.000Z", "hr_bpm": 69, "hrv_rmssd": 43.5, "hrv_sdnn": 49.0 },
            { "id": 16, "phase": "REM", "datetime": "2025-08-11T01:30:00.000Z", "hr_bpm": 67, "hrv_rmssd": 42.0, "hrv_sdnn": 47.5 },
        
            { "id": 17, "phase": "AWAKE", "datetime": "2025-08-11T01:30:00.000Z", "hr_bpm": 74, "hrv_rmssd": 31.9, "hrv_sdnn": 38.5 },
            { "id": 18, "phase": "AWAKE", "datetime": "2025-08-11T01:33:00.000Z", "hr_bpm": 73, "hrv_rmssd": 32.2, "hrv_sdnn": 39.1 },
        
            { "id": 19, "phase": "LIGHT", "datetime": "2025-08-11T01:33:00.000Z", "hr_bpm": 65, "hrv_rmssd": 41.0, "hrv_sdnn": 48.0 },
            { "id": 20, "phase": "LIGHT", "datetime": "2025-08-11T01:50:00.000Z", "hr_bpm": 64, "hrv_rmssd": 42.5, "hrv_sdnn": 49.3 },
        
            { "id": 21, "phase": "DEEP", "datetime": "2025-08-11T01:50:00.000Z", "hr_bpm": 53, "hrv_rmssd": 56.8, "hrv_sdnn": 63.8 },
            { "id": 22, "phase": "DEEP", "datetime": "2025-08-11T02:20:00.000Z", "hr_bpm": 51, "hrv_rmssd": 58.0, "hrv_sdnn": 65.0 },
        
            { "id": 23, "phase": "REM", "datetime": "2025-08-11T02:20:00.000Z", "hr_bpm": 70, "hrv_rmssd": 44.0, "hrv_sdnn": 50.0 },
            { "id": 24, "phase": "REM", "datetime": "2025-08-11T02:45:00.000Z", "hr_bpm": 68, "hrv_rmssd": 42.8, "hrv_sdnn": 48.5 },
        
            { "id": 25, "phase": "AWAKE", "datetime": "2025-08-11T06:40:00.000Z", "hr_bpm": 76, "hrv_rmssd": 30.5, "hrv_sdnn": 36.8 },
            { "id": 26, "phase": "AWAKE", "datetime": "2025-08-11T06:45:00.000Z", "hr_bpm": 75, "hrv_rmssd": 31.0, "hrv_sdnn": 37.0 }
          ],
          "createdAt": 1723334400000,
          "dataVersion": "1.0"
        }
    """.trimIndent()

        viewModelScope.launch {
            try {
                wearConnectionRepository.sendSleepJson(json)
                Log.i("SleepMonitorVM", "✅ JSON de ciclo fake enviado")
            } catch (e: Exception) {
                Log.e("SleepMonitorVM", "❌ Error enviando JSON: ${e.message}")
            }
        }
    }


}