package com.example.dashboardapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.presentation.websocket.DashboardWebSocketClient
import com.example.dashboardapp.presentation.websocket.SleepStateUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SleepStateViewModel @Inject constructor(
    @Named("sleepWebSocketUrl") private val serverUrl: String
) : ViewModel() {
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _sleepStates = MutableStateFlow<Map<String, SleepStateUpdate>>(emptyMap())
    val sleepStates: StateFlow<Map<String, SleepStateUpdate>> = _sleepStates.asStateFlow()
    
    private val _connectionMessage = MutableStateFlow("")
    val connectionMessage: StateFlow<String> = _connectionMessage.asStateFlow()
    
    private val _showConnectionError = MutableStateFlow(false)
    val showConnectionError: StateFlow<Boolean> = _showConnectionError.asStateFlow()
    
    private var webSocketClient: DashboardWebSocketClient? = null
    private var reconnectionAttempts = 0
    private val maxReconnectionAttempts = 5
    private var shouldReconnect = true
    private var hasConnectedBefore = false
    
    fun connectWebSocket() {
        viewModelScope.launch {
            if (webSocketClient?.isConnected() == true) {
                Log.d("SleepStateViewModel", "Ya está conectado")
                return@launch
            }
            
            shouldReconnect = true
            reconnectionAttempts = 0
            attemptConnection()
        }
    }
    
    private suspend fun attemptConnection() {
        try {
            if (!hasConnectedBefore) {
                _connectionMessage.value = "Conectando al servidor..."
                _showConnectionError.value = false
            }
            
            webSocketClient = DashboardWebSocketClient(
                serverUrl = serverUrl,
                onConnectionChanged = { connected ->
                    _isConnected.value = connected
                    Log.d("SleepStateViewModel", "Conexión dashboard: $connected")
                    
                    if (connected) {
                        hasConnectedBefore = true
                        reconnectionAttempts = 0
                        _showConnectionError.value = false
                        _connectionMessage.value = ""
                    } else {
                        if (hasConnectedBefore) {
                            Log.d("SleepStateViewModel", "Reconexión silenciosa en progreso...")
                        } else {
                            _connectionMessage.value = "No se pudo conectar al servidor"
                            _showConnectionError.value = true
                        }
                        
                        if (shouldReconnect) {
                            startReconnection()
                        }
                        if (hasConnectedBefore) {
                            startDisconnectionTimer()
                        }
                    }
                },
                onSleepStateUpdate = { update ->
                    updateSleepState(update)
                },
                onAllStatesReceived = { states ->
                    updateAllStates(states)
                },
                onUserDisconnected = { userId ->
                    removeUserSleepState(userId)
                }
            )
            
            webSocketClient?.connect()
        } catch (e: Exception) {
            Log.e("SleepStateViewModel", "Error en conexión: ${e.message}")
            if (!hasConnectedBefore) {
                _connectionMessage.value = "Error de conexión: ${e.message}"
                _showConnectionError.value = true
            }
            if (shouldReconnect) {
                startReconnection()
            }
        }
    }
    
    private fun startReconnection() {
        if (reconnectionAttempts >= maxReconnectionAttempts) {
            if (!hasConnectedBefore) {
                _connectionMessage.value = "No se pudo conectar al servidor después de $maxReconnectionAttempts intentos"
                _showConnectionError.value = true
            }
            Log.e("SleepStateViewModel", "Máximo de intentos de reconexión alcanzado")
            return
        }
        
        viewModelScope.launch {
            reconnectionAttempts++
            val delayTime = minOf(2000L * reconnectionAttempts, 30000L)
            
            if (!hasConnectedBefore) {
                _connectionMessage.value = "Reintentando conexión... (${reconnectionAttempts}/$maxReconnectionAttempts)"
            }
            Log.d("SleepStateViewModel", "Reintentando conexión en ${delayTime}ms, intento: $reconnectionAttempts")
            
            delay(delayTime)
            
            if (shouldReconnect && !_isConnected.value) {
                attemptConnection()
            }
        }
    }
    
    private fun startDisconnectionTimer() {
        viewModelScope.launch {
            delay(30000) // 30 segundos
            if (!_isConnected.value) {
                // Si sigue desconectado después de 30s, limpiar estados antiguos
                cleanOldStates()
            }
        }
    }
    
    private fun cleanOldStates() {
        val currentTime = LocalDateTime.now()
        val currentStates = _sleepStates.value.toMutableMap()
        
        currentStates.entries.removeAll { (_, state) ->
            try {
                val stateTime = LocalDateTime.parse(state.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                currentTime.minusMinutes(2).isAfter(stateTime) // Remover estados de más de 2 minutos
            } catch (e: Exception) {
                false // Mantener si no se puede parsear la fecha
            }
        }
        
        _sleepStates.value = currentStates
        Log.d("SleepStateViewModel", "Estados antiguos limpiados")
    }
    
    fun disconnectWebSocket() {
        shouldReconnect = false
        webSocketClient?.disconnect()
        webSocketClient = null
        _isConnected.value = false
        _sleepStates.value = emptyMap()
        _connectionMessage.value = "Desconectado"
    }
    
    fun forceReconnect() {
        viewModelScope.launch {
            Log.d("SleepStateViewModel", "Forzando reconexión...")
            _connectionMessage.value = "Forzando reconexión..."
            disconnectWebSocket()
            delay(1000) // Esperar un segundo antes de reconectar
            connectWebSocket()
        }
    }
    
    private fun updateSleepState(update: SleepStateUpdate) {
        val currentStates = _sleepStates.value.toMutableMap()
        currentStates[update.userId] = update
        _sleepStates.value = currentStates
        Log.d("SleepStateViewModel", "Estado actualizado para ${update.userId}: ${update.sleepState}")
    }
    
    private fun removeUserSleepState(userId: String) {
        val currentStates = _sleepStates.value.toMutableMap()
        currentStates.remove(userId)
        _sleepStates.value = currentStates
        Log.d("SleepStateViewModel", "Usuario removido: $userId")
    }
    
    private fun updateAllStates(states: List<SleepStateUpdate>) {
        val statesMap = states.associateBy { it.userId }
        _sleepStates.value = statesMap
        Log.d("SleepStateViewModel", "Estados iniciales cargados: ${states.size}")
    }
    
    fun getSleepStateForUser(userId: String): SleepStateUpdate? {
        return _sleepStates.value[userId]
    }
    
    override fun onCleared() {
        super.onCleared()
        shouldReconnect = false
        disconnectWebSocket()
    }
}
