package com.example.wereableapp.presentation.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Manager para leer RR Intervals y calcular HRV.
 * Asume que el sensor de tipo HEART_BEAT devuelve RR Intervals (milisegundos).
 */
class HRVSensorManager(
    private val context: Context,
    private val onRRIntervalsReady: (List<Long>) -> Unit
) {

    // Mantiene la lista de RR generados
    private var rrIntervals = mutableListOf<Long>()

    // Usado para acumular mientras el sensor de HR esté activo
    fun onNewBPM(bpm: Float) {
        val rr = (60_000 / bpm).toLong()
        rrIntervals.add(rr)

        Log.d("HRVSensorManager", "RR simulado: $rr ms (BPM: $bpm)")

        if (rrIntervals.size >= 20) {
            onRRIntervalsReady(rrIntervals.toList())
            rrIntervals.clear()
        }
    }

    fun start() {
        Log.d("HRVSensorManager", "Iniciado simulador HRV desde BPM.")
    }

    fun stop() {
        rrIntervals.clear()
        Log.d("HRVSensorManager", "Detenido simulador HRV.")
    }
}
