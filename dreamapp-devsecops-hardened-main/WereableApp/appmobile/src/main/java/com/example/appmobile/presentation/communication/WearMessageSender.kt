package com.example.appmobile.presentation.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WearMessageSender(private val context: Context) {

    suspend fun sendUserDataToWear(
        edad: Int,
        peso: Float,
        estatura: Float,
        sexo: String
    ) = withContext(Dispatchers.IO) {
        try {
            val messageClient: MessageClient = Wearable.getMessageClient(context)

            val nodeListTask = Wearable.getNodeClient(context).connectedNodes
            val nodes = Tasks.await(nodeListTask)

            val message = "$edad|$peso|$estatura|$sexo"
            val path = "/user_data"

            for (node in nodes) {
                val sendTask = messageClient.sendMessage(node.id, path, message.toByteArray())
                Tasks.await(sendTask)
                Log.d("WearMessageSender", "✅ Datos enviados al nodo ${node.displayName}: $message")
            }
        } catch (e: Exception) {
            Log.e("WearMessageSender", "❌ Error al enviar datos: ${e.message}")
        }
    }
}
