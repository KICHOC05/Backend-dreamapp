package com.example.wereableapp.presentation.data.repository

import android.content.Context
import android.util.Log
import com.example.wereableapp.presentation.data.sensor.AccelerometerSensorManager
import com.example.wereableapp.presentation.data.sensor.HRVSensorManager
import com.example.wereableapp.presentation.data.sensor.HeartRateSensorManager
import com.example.wereableapp.presentation.domain.model.Accelerometer
import com.example.wereableapp.presentation.domain.model.HeartRate
import com.example.wereableapp.presentation.domain.model.HeartRateV
import com.example.wereableapp.presentation.domain.repository.SensorRepository
import com.example.wereableapp.presentation.domain.usecase.SleepPhaseDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación de SensorRepository.
 * Gestiona:
 *  - Lectura de sensores físicos (HR + acelerometro)
 *  - Transformación a flujos reactivos
 *  - Cálculo de magnitud de movimiento
 *  - Estimación de fase del sueño
 */
class SensorRepositoryImpl(
    private val context: Context
) : SensorRepository {

    // Estado actual de la frecuencia cardíaca y el acelerometro (XYZ)
    private val heartRateFlow = MutableStateFlow<HeartRate?>(null)
    private val accelerometerFlow = MutableStateFlow<Accelerometer?>(null)
    private val hrvFlow = MutableStateFlow<HeartRateV?>(null)

    // Magnitud de movimiento calculada a partir del acelerómetro
    private val movementMagnitudeFlow = MutableStateFlow(0.0)

    // Sensor de HR: actualiza flujo cuando recibe BPM
    private val hrManager = HeartRateSensorManager(context) { bpm ->
        bpm?.let {
            val ts = System.currentTimeMillis()
            heartRateFlow.value = HeartRate(it, ts)

            // Simular RR derivado del BPM
            hrvManager.onNewBPM(it)

            Log.d("SensorRepo", "BPM: $it enviado a HRVManager")
        }
    }

    // Sensor de acelerómetro: actualiza XYZ y magnitud
    private val accelerometerManager = AccelerometerSensorManager(context) { x, y, z ->
        accelerometerFlow.value = Accelerometer(x, y, z, System.currentTimeMillis())
        movementMagnitudeFlow.value = Math.sqrt((x * x + y * y + z * z).toDouble())
    }

    // Detector de fases de sueño usando HR + movimiento
    private val sleepPhaseDetector = SleepPhaseDetector(
        heartRateFlow.asStateFlow(),
        movementMagnitudeFlow.asStateFlow(),
        hrvFlow.asStateFlow() // ✅ lo pasas también
    )

    private val hrvManager = HRVSensorManager(context) { rrList ->
        val rmssd = calculateRMSSD(rrList)
        val sdnn = calculateSDNN(rrList)
        hrvFlow.value = HeartRateV(rrList, rmssd, sdnn, System.currentTimeMillis())
    }

    override fun hrvFlow(): Flow<HeartRateV?> = hrvFlow.asStateFlow()

    override fun heartRateFlow() = heartRateFlow.asStateFlow()
    override fun accelerometerFlow() = accelerometerFlow.asStateFlow()
    override fun sleepPhaseFlow() = sleepPhaseDetector.sleepPhaseFlow

    // Inicia los sensores
    override fun startSensors() {
        hrManager.start()
        accelerometerManager.start()
        hrvManager.start()
    }

    // Detiene los sensores
    override fun stopSensors() {
        hrManager.stop()
        accelerometerManager.stop()
        hrvManager.stop()
    }
    private fun filterValidRR(rr: List<Long>): List<Long> {
        return rr.filter { it in 300..2000 }
    }


    private fun calculateRMSSD(rr: List<Long>): Double {
        val validRR = filterValidRR(rr)

        if (validRR.size < 2) {
            Log.w("SleepPhaseDetector", "❌ No hay suficientes RR válidos para RMSSD: $rr")
            return 0.0
        }

        val diffs = validRR.zipWithNext { a, b -> (b - a).toDouble() }
        val squaredDiffs = diffs.map { it * it }
        val rmssd = kotlin.math.sqrt(squaredDiffs.average())

        Log.d("SleepPhaseDetector", "🔁 RMSSD: $rmssd, RR: $validRR")

        return rmssd
    }


    private fun calculateSDNN(rr: List<Long>): Double {
        val validRR = filterValidRR(rr)

        if (validRR.size < 2) {
            Log.w("SleepPhaseDetector", "❌ No hay suficientes RR válidos para SDNN: $rr")
            return 0.0
        }

        val rrDoubles = validRR.map { it.toDouble() }
        val mean = rrDoubles.average()
        val squaredDiffs = rrDoubles.map { (it - mean) * (it - mean) }

        val sdnn = kotlin.math.sqrt(squaredDiffs.average())

        Log.d("SleepPhaseDetector", "📊 SDNN: $sdnn, RR: $validRR")

        return sdnn
    }


}
