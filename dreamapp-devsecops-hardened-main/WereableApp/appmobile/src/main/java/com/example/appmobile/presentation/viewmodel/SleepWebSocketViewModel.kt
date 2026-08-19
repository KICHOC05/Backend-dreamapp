package com.example.appmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appmobile.presentation.websocket.SleepStateEnum
import com.example.appmobile.presentation.websocket.SleepStateWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SleepWebSocketViewModel : ViewModel() {
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _isSyncEnabled = MutableStateFlow(false)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()
    
    private val _lastMessage = MutableStateFlow("")
    val lastMessage: StateFlow<String> = _lastMessage.asStateFlow()
    
    private val _currentSleepState = MutableStateFlow<SleepStateEnum?>(null)
    val currentSleepState: StateFlow<SleepStateEnum?> = _currentSleepState.asStateFlow()
    
    private var webSocketClient: SleepStateWebSocketClient? = null
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    
    // URL del servidor - usar 10.0.2.2 para emulador o la IP de tu PC para dispositivo físico
    private val serverUrl = "${com.example.appmobile.BuildConfig.WS_BASE_URL}/ws/sleep/mobile"
    
    fun toggleSync() {
        viewModelScope.launch {
            if (_isSyncEnabled.value) {
                stopSync()
            } else {
                startSync()
            }
        }
    }
    
    fun startSync() {
        Log.d("SleepWebSocket", "Iniciando sincronización con URL: $serverUrl")
        webSocketClient = SleepStateWebSocketClient(
            serverUrl = serverUrl,
            onConnectionChanged = { connected ->
                Log.d("SleepWebSocket", "Estado de conexión cambió: $connected")
                _isConnected.value = connected
                if (!connected) {
                    _isSyncEnabled.value = false
                    _currentSleepState.value = null
                }
            },
            onMessageReceived = { message ->
                _lastMessage.value = message
                Log.d("SleepWebSocket", "Mensaje recibido: $message")
            }
        )
        
        webSocketClient?.connect()
        _isSyncEnabled.value = true
    }
    
    fun stopSync() {
        // Enviar mensaje de desconexión antes de cerrar
        if (_isConnected.value && currentUserId != null && currentUserName != null) {
            sendDisconnectionMessage(currentUserId!!, currentUserName!!)
        }
        
        webSocketClient?.disconnect()
        webSocketClient = null
        _isSyncEnabled.value = false
        _isConnected.value = false
        _currentSleepState.value = null
        currentUserId = null
        currentUserName = null
    }
    
    private fun sendDisconnectionMessage(userId: String, userName: String) {
        try {
            val disconnectMessage = mapOf(
                "action" to "disconnect",
                "userId" to userId,
                "userName" to userName,
                "timestamp" to System.currentTimeMillis()
            )
            webSocketClient?.sendMessage(disconnectMessage)
            Log.d("SleepWebSocketViewModel", "Enviado mensaje de desconexión para usuario: $userName")
        } catch (e: Exception) {
            Log.e("SleepWebSocketViewModel", "Error enviando mensaje de desconexión", e)
        }
    }
    
    fun sendSleepState(
        userId: String,
        userName: String,
        sleepState: SleepStateEnum
    ) {
        if (!_isConnected.value) {
            Log.w("SleepWebSocket", "No hay conexión activa")
            return
        }
        
        // Guardar información del usuario para posible desconexión
        currentUserId = userId
        currentUserName = userName
        
        webSocketClient?.sendSleepState(
            userId = userId,
            userName = userName,
            sleepState = sleepState,
            deviceId = "android_device_${System.currentTimeMillis()}"
        )
        
        _currentSleepState.value = sleepState
    }
    
    override fun onCleared() {
        super.onCleared()
        stopSync()
    }
}

class SleepWebSocketViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepWebSocketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepWebSocketViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
