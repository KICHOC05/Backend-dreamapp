package com.example.wereableapp.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wereableapp.R
import com.example.wereableapp.presentation.data.buffer.SensorDataBuffer
import com.example.wereableapp.presentation.data.sensor.HeartRateSensorManager
import com.example.wereableapp.presentation.data.sensor.AccelerometerSensorManager


class SensorForegroundService : Service() {

    private lateinit var heartRateSensor: HeartRateSensorManager
    private lateinit var accelerometerSensor: AccelerometerSensorManager

    override fun onCreate() {
        super.onCreate()

        heartRateSensor = HeartRateSensorManager(this) { bpm ->
            SensorDataBuffer.updateHeartRate(bpm)
        }

        accelerometerSensor = AccelerometerSensorManager(this) { x, y, z ->
            SensorDataBuffer.updateAccelerometer(x, y, z)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("Monitoreando sensores..."))
        heartRateSensor.start()
        accelerometerSensor.start()
        return START_STICKY
    }

    override fun onDestroy() {
        heartRateSensor.stop()
        accelerometerSensor.stop()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(contentText: String): Notification {
        val channelId = "sensor_channel"
        val channelName = "Monitoreo de sensores"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Servicio de Monitoreo")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // usa tu ícono
            .setOngoing(true)
            .build()
    }
}
