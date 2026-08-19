package com.example.appmobile.presentation.communication

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.appmobile.data.repository.HeartRateRepository
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d("WearDataListener", "Servicio iniciado")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WearDataListener", "Mensaje recibido con path: ${messageEvent.path}")

        if (messageEvent.path == "/heart_rate") {
            val data = String(messageEvent.data)
            val bpm = data.toFloatOrNull()

            if (bpm != null) {
                Log.d("WearDataListener", "BPM recibido: $bpm")
                mainHandler.post {
                    HeartRateRepository.updateBPM(bpm)
                }
            } else {
                Log.e("WearDataListener", "No se pudo convertir '$data' a Float")
            }
        } else {
            Log.w("WearDataListener", "Mensaje con path inesperado: ${messageEvent.path}")
        }
    }
}