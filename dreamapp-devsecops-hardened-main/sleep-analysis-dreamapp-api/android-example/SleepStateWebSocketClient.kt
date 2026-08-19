// SleepStateWebSocketClient.kt
// Este archivo muestra cómo implementar el cliente WebSocket en tu app Android

package com.example.dreamapp.websocket

import android.util.Log
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SleepStateWebSocketClient(
    private val serverUrl: String,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onMessageReceived: (String) -> Unit
) {
    
    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()
    private val tag = "SleepWebSocket"
    
    fun connect() {
        try {
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.d(tag, "Conectado al servidor WebSocket")
                    onConnectionChanged(true)
                }
                
                override fun onMessage(message: String?) {
                    Log.d(tag, "Mensaje recibido: $message")
                    message?.let { onMessageReceived(it) }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(tag, "Conexión cerrada: $reason")
                    onConnectionChanged(false)
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(tag, "Error en WebSocket: ${ex?.message}")
                    onConnectionChanged(false)
                }
            }
            
            webSocketClient?.connect()
        } catch (e: Exception) {
            Log.e(tag, "Error al conectar: ${e.message}")
            onConnectionChanged(false)
        }
    }
    
    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        onConnectionChanged(false)
    }
    
    fun sendSleepState(
        userId: String,
        userName: String,
        sleepState: SleepStateEnum,
        deviceId: String? = null,
        location: LocationData? = null
    ) {
        if (webSocketClient?.isOpen != true) {
            Log.w(tag, "WebSocket no está conectado")
            return
        }
        
        val message = SleepStateMessage(
            action = "changeSleepState",
            userId = userId,
            userName = userName,
            sleepState = sleepState.name,
            deviceId = deviceId,
            location = location
        )
        
        val jsonMessage = gson.toJson(message)
        webSocketClient?.send(jsonMessage)
        Log.d(tag, "Estado enviado: $jsonMessage")
    }
    
    fun isConnected(): Boolean = webSocketClient?.isOpen == true
}

// Enums y data classes
enum class SleepStateEnum(val displayName: String, val colorCode: String) {
    REM("REM", "#FF6B6B"),
    LIGHT("Light", "#4ECDC4"),
    DEEP("Deep", "#45B7D1"),
    AWAKE("Awake", "#96CEB4")
}

data class SleepStateMessage(
    val action: String,
    val userId: String,
    val userName: String,
    val sleepState: String,
    val deviceId: String? = null,
    val location: LocationData? = null
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class SleepStateResponse(
    val status: String,
    val message: String,
    val sleepState: String? = null,
    val timestamp: String? = null
)
