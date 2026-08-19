package com.example.wereableapp.presentation.data.communication

import android.content.Context
import android.util.Log
import com.example.wereableapp.presentation.domain.repository.WearConnectionRepository
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearConnectionImpl(
    private val context: Context
) : WearConnectionRepository {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    override suspend fun sendHeartRate(bpm: Float) {
        Log.i("WearConnectionImpl", "💓 Enviando HR: $bpm BPM")
        sendMessage("/heart_rate", bpm.toString())
    }

    override suspend fun sendHRV(rmssd: Double, sdnn: Double) {
        val payload = "RMSSD:$rmssd;SDNN:$sdnn"
        Log.i("WearConnectionImpl", "📊 Enviando HRV: $payload")
        sendMessage("/hrv", payload)
    }

    override suspend fun sendSleepPhase(phase: String) {
        Log.i("WearConnectionImpl", "😴 Enviando fase: $phase")
        sendMessage("/sleep_phase", phase)
    }

    override suspend fun sendSleepJson(json: String) {
        Log.i("WearConnectionImpl", "📤 Enviando JSON completo de sueño")
        sendMessage("/sleep_full_data", json)
    }

    private suspend fun sendMessage(path: String, payload: String) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            Log.d("WearConnectionImpl", "🔍 Nodos conectados encontrados: ${nodes.size}")

            if (nodes.isEmpty()) {
                Log.w("WearConnectionImpl", "⚠️ No hay nodos conectados")
                return
            }

            for (node in nodes) {
                Log.d("WearConnectionImpl", "📡 Enviando a nodo: ${node.displayName} (${node.id})")
                messageClient.sendMessage(
                    node.id,
                    path,
                    payload.toByteArray()
                ).await()
                Log.d("WearConnectionImpl", "✅ Sent [$path] → ${node.displayName} | Data: $payload")
            }
        } catch (e: Exception) {
            Log.e("WearConnectionImpl", "❌ Error sending message [$path]: ${e.message}", e)
        }
    }
}