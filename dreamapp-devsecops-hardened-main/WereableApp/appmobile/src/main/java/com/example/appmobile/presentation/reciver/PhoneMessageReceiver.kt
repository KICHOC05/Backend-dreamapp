package com.example.appmobile.presentation.reciver

import android.content.Intent
import android.util.Log
import com.example.appmobile.data.database.SleepDatabase
import com.example.appmobile.data.database.entity.SleepCycleEntity
import com.example.appmobile.data.database.entity.SleepPhaseDataEntity
import com.example.appmobile.presentation.shared.PhoneDataHolder
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson


/**
 * Escucha mensajes enviados desde el Wearable.
 */
data class SleepPhaseData(
    val id: Int,
    val phase: String,
    val datetime: String,
    val hr_bpm: Int,
    val hrv_rmssd: Double,
    val hrv_sdnn: Double
)

data class SleepCycle(
    val uidUser: String,
    val deviceId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val timezone: String,
    val totalDuration: Int,
    val sleepDuration: Int,
    val lightSleepMinutes: Int,
    val deepSleepMinutes: Int,
    val remSleepMinutes: Int,
    val awakeDuration: Int,
    val sleepEfficiency: Double,
    val awakeningsCount: Int,
    val quality: String,
    val avgHeartRate: Int,
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val avgMovement: Int,
    val avgRmssd: Double,
    val avgSdnn: Double,
    val sleepPhaseData: List<SleepPhaseData>,
    val createdAt: Long,
    val dataVersion: String
)
class PhoneMessageReceiver : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.i("PhoneReceiver", "🚀 PhoneMessageReceiver iniciado")
    }

    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)
        Log.i("PhoneReceiver", "✅ Mensaje recibido PATH: ${event.path}")
        val dataString = String(event.data)
        Log.d("PhoneReceiver", "📦 Datos recibidos: $dataString")
        Log.d("PhoneReceiver", "🌐 Nodo origen: ${event.sourceNodeId}")
        Log.d("PhoneReceiver", "📏 Tamaño datos: ${event.data.size} bytes")

        when (event.path) {
            "/heart_rate" -> {
                val bpm = dataString.toFloatOrNull()
                Log.i("PhoneReceiver", "💓 Procesando HR raw: '$dataString' -> parsed: $bpm")
                bpm?.let {
                    Log.i("PhoneReceiver", "📲 Actualizando HR: $it")
                    runOnUiThread {
                        PhoneDataHolder.heartRate.value = it
                        Log.i("PhoneReceiver", "✅ HR actualizado en holder: ${PhoneDataHolder.heartRate.value}")
                    }
                } ?: Log.w("PhoneReceiver", "❌ No se pudo parsear HR: '$dataString'")
            }
            "/hrv" -> {
                Log.i("PhoneReceiver", "📲 Actualizando HRV: $dataString")
                runOnUiThread {
                    PhoneDataHolder.hrv.value = dataString
                    Log.i("PhoneReceiver", "✅ HRV actualizado en holder: ${PhoneDataHolder.hrv.value}")
                }
            }
            "/sleep_phase" -> {
                Log.i("PhoneReceiver", "📲 Actualizando Phase: $dataString")
                runOnUiThread {
                    PhoneDataHolder.sleepPhase.value = dataString
                    Log.i("PhoneReceiver", "✅ Phase actualizado en holder: ${PhoneDataHolder.sleepPhase.value}")
                }
            }
            "/sleep_full_data" -> {
                try {
                    val cycle = Gson().fromJson(dataString, SleepCycle::class.java)

                    val cycleEntity = SleepCycleEntity(
                        createdAt = cycle.createdAt,
                        uidUser = cycle.uidUser,
                        deviceId = cycle.deviceId,
                        date = cycle.date,
                        startTime = cycle.startTime,
                        endTime = cycle.endTime,
                        timezone = cycle.timezone,
                        totalDuration = cycle.totalDuration,
                        sleepDuration = cycle.sleepDuration,
                        lightSleepMinutes = cycle.lightSleepMinutes,
                        deepSleepMinutes = cycle.deepSleepMinutes,
                        remSleepMinutes = cycle.remSleepMinutes,
                        awakeDuration = cycle.awakeDuration,
                        sleepEfficiency = cycle.sleepEfficiency,
                        awakeningsCount = cycle.awakeningsCount,
                        quality = cycle.quality,
                        avgHeartRate = cycle.avgHeartRate,
                        minHeartRate = cycle.minHeartRate,
                        maxHeartRate = cycle.maxHeartRate,
                        avgMovement = cycle.avgMovement,
                        avgRmssd = cycle.avgRmssd,
                        avgSdnn = cycle.avgSdnn,
                        dataVersion = cycle.dataVersion
                    )

                    val phasesEntity = cycle.sleepPhaseData.map {
                        SleepPhaseDataEntity(
                            id = it.id,
                            parentCreatedAt = cycle.createdAt,
                            phase = it.phase,
                            datetime = it.datetime,
                            hr_bpm = it.hr_bpm,
                            hrv_rmssd = it.hrv_rmssd,
                            hrv_sdnn = it.hrv_sdnn
                        )
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        val db = SleepDatabase.getDatabase(applicationContext)
                        db.sleepCycleDao().insertSleepCycle(cycleEntity)
                        db.sleepPhaseDataDao().insertSleepPhaseData(phasesEntity)
                        Log.i("PhoneReceiver", "✅ Sleep cycle y fases guardados en BD")
                    }
                } catch (e: Exception) {
                    Log.e("PhoneReceiver", "❌ Error parseando o guardando JSON: ${e.message}")
                }
            }
            else -> {
                Log.w("PhoneReceiver", "⚠️ Path no reconocido: '${event.path}', datos: '$dataString'")
            }
        }
    }

    private fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post(action)
    }
}