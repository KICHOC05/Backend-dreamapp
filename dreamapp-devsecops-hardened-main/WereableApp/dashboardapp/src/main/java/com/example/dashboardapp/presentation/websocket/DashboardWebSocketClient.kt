package com.example.dashboardapp.presentation.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class DashboardWebSocketClient(
    private val serverUrl: String,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onSleepStateUpdate: (SleepStateUpdate) -> Unit,
    private val onAllStatesReceived: (List<SleepStateUpdate>) -> Unit,
    private val onUserDisconnected: (String) -> Unit = {} // Callback para desconexión de usuario
) {
    
    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()
    private val tag = "DashboardWebSocket"
    
    fun connect() {
        try {
            val uri = URI(serverUrl)
            Log.d(tag, "Conectando al dashboard WebSocket: $serverUrl")
            
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.d(tag, "✅ Conectado al dashboard WebSocket")
                    onConnectionChanged(true)
                }
                
                override fun onMessage(message: String?) {
                    Log.d(tag, "📨 Mensaje recibido: $message")
                    message?.let { handleMessage(it) }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(tag, "❌ Dashboard WebSocket cerrado - Código: $code, Razón: $reason")
                    onConnectionChanged(false)
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(tag, "🚨 Error en dashboard WebSocket: ${ex?.message}", ex)
                    onConnectionChanged(false)
                }
            }
            
            webSocketClient?.connect()
        } catch (e: Exception) {
            Log.e(tag, "💥 Error al crear dashboard WebSocket: ${e.message}", e)
            onConnectionChanged(false)
        }
    }
    
    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        onConnectionChanged(false)
    }
    
    private fun handleMessage(message: String) {
        try {
            val messageMap = gson.fromJson(message, Map::class.java) as Map<String, Any>
            
            when (messageMap["action"]) {
                "allStates" -> {
                    val statesArray = messageMap["states"] as? List<Map<String, Any>> ?: emptyList()
                    val sleepStates = statesArray.mapNotNull { parseState(it) }
                    onAllStatesReceived(sleepStates)
                    Log.d(tag, "Estados iniciales recibidos: ${sleepStates.size}")
                }
                
                "stateChange" -> {
                    val eventMap = messageMap["event"] as? Map<String, Any>
                    if (eventMap != null) {
                        parseState(eventMap)?.let { state ->
                            onSleepStateUpdate(state)
                            Log.d(tag, "Estado actualizado: ${state.userId} -> ${state.sleepState}")
                        }
                    }
                }
                
                "userDisconnected" -> {
                    val userId = messageMap["userId"] as? String
                    val userName = messageMap["userName"] as? String
                    if (userId != null) {
                        onUserDisconnected(userId)
                        Log.d(tag, "Usuario desconectado: $userId ($userName)")
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(tag, "Error parsing JSON: ${e.message}")
        } catch (e: Exception) {
            Log.e(tag, "Error handling message: ${e.message}")
        }
    }
    
    private fun parseState(stateMap: Map<String, Any>): SleepStateUpdate? {
        return try {
            SleepStateUpdate(
                userId = stateMap["userId"] as? String ?: return null,
                userName = stateMap["userName"] as? String ?: "",
                sleepState = stateMap["sleepState"] as? String ?: return null,
                sleepStateDisplay = stateMap["sleepStateDisplay"] as? String ?: "",
                colorCode = stateMap["colorCode"] as? String ?: "#000000",
                timestamp = stateMap["timestamp"] as? String ?: "",
                deviceId = stateMap["deviceId"] as? String
            )
        } catch (e: Exception) {
            Log.e(tag, "Error parsing state: ${e.message}")
            null
        }
    }
    
    fun isConnected(): Boolean = webSocketClient?.isOpen == true
}

data class SleepStateUpdate(
    val userId: String,
    val userName: String,
    val sleepState: String,
    val sleepStateDisplay: String,
    val colorCode: String,
    val timestamp: String,
    val deviceId: String? = null
)

enum class SleepStateEnum(val displayName: String, val colorCode: String, val icon: String) {
    REM("REM", "#FF6B6B", "🔴"),
    LIGHT("Sueño Ligero", "#4ECDC4", "🟢"),
    DEEP("Sueño Profundo", "#45B7D1", "🔵"),
    AWAKE("Despierto", "#96CEB4", "🟡");
    
    companion object {
        fun fromString(value: String): SleepStateEnum? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
