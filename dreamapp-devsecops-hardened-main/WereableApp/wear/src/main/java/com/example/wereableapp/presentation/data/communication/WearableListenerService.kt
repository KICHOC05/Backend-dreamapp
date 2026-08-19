package com.example.wereableapp.presentation.data.communication

import android.util.Log
import com.example.wereableapp.presentation.data.repository.UserRepository
import com.example.wereableapp.presentation.domain.model.UserData
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

// En el WEARABLE
class WearMessageReceiver : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/user_data") {
            val payload = String(messageEvent.data, Charsets.UTF_8)
            Log.d("WearMessageReceiver", "📩 Datos recibidos: $payload")

            val parts = payload.split("|")
            val edad = parts.getOrNull(0)?.toIntOrNull()
            val peso = parts.getOrNull(1)?.toFloatOrNull()
            val estatura = parts.getOrNull(2)?.toFloatOrNull()
            val sexo = parts.getOrNull(3)?.trim()

            if (edad != null && peso != null && estatura != null && !sexo.isNullOrEmpty()) {
                val userData = UserData(edad, peso, estatura, sexo)
                UserRepository.saveUserData(userData)

                Log.d("WearMessageReceiver", "✅ Usuario guardado en UserRepository: $userData")
            } else {
                Log.e("WearMessageReceiver", "❌ Error al parsear datos de usuario: $payload")
            }
        }
    }
}
