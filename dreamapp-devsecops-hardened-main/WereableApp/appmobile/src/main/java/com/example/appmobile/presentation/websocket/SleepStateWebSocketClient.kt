package com.example.appmobile.presentation.websocket

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
            // Cerrar conexión existente si la hay
            disconnect()
            
            val uri = URI(serverUrl)
            Log.d(tag, "Intentando conectar a: $serverUrl")
            Log.d(tag, "URI parsed: $uri")
            
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.d(tag, "✅ Conectado al servidor WebSocket")
                    Log.d(tag, "Handshake: ${handshake?.httpStatusMessage}")
                    onConnectionChanged(true)
                }
                
                override fun onMessage(message: String?) {
                    Log.d(tag, "📨 Mensaje recibido: $message")
                    message?.let { onMessageReceived(it) }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(tag, "❌ Conexión cerrada - Código: $code, Razón: $reason, Remoto: $remote")
                    onConnectionChanged(false)
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(tag, "🚨 Error en WebSocket: ${ex?.message}", ex)
                    Log.e(tag, "Tipo de error: ${ex?.javaClass?.simpleName}")
                    onConnectionChanged(false)
                }
            }
            
            // Configurar timeouts para evitar conexiones colgadas
            webSocketClient?.connectionLostTimeout = 30 // 30 segundos
            
            Log.d(tag, "Iniciando conexión WebSocket...")
            webSocketClient?.connect()
            Log.d(tag, "Comando de conexión enviado")
        } catch (e: Exception) {
            Log.e(tag, "💥 Error al crear cliente WebSocket: ${e.message}", e)
            onConnectionChanged(false)
        }
    }
    
    fun disconnect() {
        try {
            webSocketClient?.let { client ->
                if (client.isOpen) {
                    Log.d(tag, "Cerrando conexión WebSocket activa...")
                    client.close(1000, "Cliente desconectándose normalmente")
                } else {
                    Log.d(tag, "Conexión WebSocket ya estaba cerrada")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error al cerrar WebSocket: ${e.message}", e)
        } finally {
            webSocketClient = null
            onConnectionChanged(false)
        }
    }
    
    fun sendSleepState(
        userId: String,
        userName: String,
        sleepState: SleepStateEnum,
        deviceId: String? = null,
        location: LocationData? = null
    ) {
        try {
            if (webSocketClient?.isOpen != true) {
                Log.w(tag, "WebSocket no está conectado, no se puede enviar estado")
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
        } catch (e: Exception) {
            Log.e(tag, "Error enviando estado de sueño: ${e.message}", e)
        }
    }
    
    fun sendMessage(message: Map<String, Any>) {
        try {
            if (webSocketClient?.isOpen != true) {
                Log.w(tag, "WebSocket no está conectado, no se puede enviar mensaje")
                return
            }
            
            val jsonMessage = gson.toJson(message)
            webSocketClient?.send(jsonMessage)
            Log.d(tag, "Mensaje enviado: $jsonMessage")
        } catch (e: Exception) {
            Log.e(tag, "Error enviando mensaje: ${e.message}", e)
        }
    }
    
    fun isConnected(): Boolean = webSocketClient?.isOpen == true
}

// Enums y data classes
enum class SleepStateEnum(val displayName: String, val colorCode: String, val icon: String) {
    REM("REM", "#FF6B6B", "🔴"),
    LIGHT("Sueño Ligero", "#4ECDC4", "🟢"),
    DEEP("Sueño Profundo", "#45B7D1", "🔵"),
    AWAKE("Despierto", "#96CEB4", "🟡")
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
