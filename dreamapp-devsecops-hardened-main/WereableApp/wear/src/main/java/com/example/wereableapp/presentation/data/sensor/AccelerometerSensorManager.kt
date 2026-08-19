package com.example.wereableapp.presentation.data.sensor

/*
* El codigo implementa un SensorManager para el sensor de acelerometro.
* */

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class AccelerometerSensorManager(
    private val context: Context,
    private val onMovementDetected: (x: Float, y: Float, z: Float) -> Unit
) {

    @SuppressLint("ServiceCast")
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // ✅ Redondea valores antes del envio a 1 decimal despues del .
            val xRounded = ((x * 10).toInt()) / 10f
            val yRounded = ((y * 10).toInt()) / 10f
            val zRounded = ((z * 10).toInt()) / 10f

            Log.d("AccelerometerSensor", "x: $xRounded, y: $yRounded, z: $zRounded")

            onMovementDetected(xRounded, yRounded, zRounded)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
    }

    /*
    * Inicia la lectura del sensor
    * */
    fun start() {
        if (accelerometer == null) {
            Log.e("AccelerometerSensor", "❌ Sensor de acelerómetro no disponible.")
            return
        }
        Log.d("AccelerometerSensor", "✅ Registrando listener de acelerómetro.")
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /*
    * Detiene la lectura del sensor
    * */
    fun stop() {
        Log.d("AccelerometerSensor", "⏹️ Deteniendo listener de acelerómetro.")
        sensorManager.unregisterListener(listener)
    }
}