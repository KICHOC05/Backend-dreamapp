package com.example.wereableapp.presentation.data.buffer

object SensorDataBuffer {
    var heartRate: Float? = null
    var accX: Float? = null
    var accY: Float? = null
    var accZ: Float? = null

    fun updateHeartRate(value: Float?) {
        heartRate = value
    }

    fun updateAccelerometer(x: Float, y: Float, z: Float) {
        accX = x
        accY = y
        accZ = z
    }

    fun clear() {
        heartRate = null
        accX = null
        accY = null
        accZ = null
    }
}
