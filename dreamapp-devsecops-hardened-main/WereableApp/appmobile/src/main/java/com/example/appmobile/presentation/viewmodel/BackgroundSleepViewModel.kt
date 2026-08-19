package com.example.appmobile.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.presentation.service.SleepServiceManager
import com.example.appmobile.presentation.websocket.SleepStateEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackgroundSleepViewModel(application: Application) : AndroidViewModel(application) {
    
    private val serviceManager = SleepServiceManager(application)
    
    // Estados locales
    private val _isSyncEnabled = MutableStateFlow(false)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _currentSleepState = MutableStateFlow<SleepStateEnum?>(null)
    val currentSleepState: StateFlow<SleepStateEnum?> = _currentSleepState.asStateFlow()
    
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    
    init {
        // Bind al servicio al inicializar
        serviceManager.bindToService()
        
        // Observar estados del servicio
        observeServiceStates()
    }
    
    private fun observeServiceStates() {
        observeConnectionState()
        observeMonitoringState()
        observeSleepState()
    }
    
    private fun reObserveServiceStates() {
        Log.d("BackgroundSleepVM", "Re-observando estados del servicio")
        // Cancelar observaciones anteriores si las hay
        observeServiceStates()
    }
    
    private fun observeConnectionState() {
        viewModelScope.launch {
            while (!serviceManager.isServiceBound()) {
                kotlinx.coroutines.delay(100)
            }
            
            serviceManager.getConnectionState()?.collect { connected ->
                _isConnected.value = connected
                Log.d("BackgroundSleepVM", "Conexión cambió: $connected")
            }
        }
    }
    
    private fun observeMonitoringState() {
        viewModelScope.launch {
            while (!serviceManager.isServiceBound()) {
                kotlinx.coroutines.delay(100)
            }
            
            serviceManager.getMonitoringState()?.collect { monitoring ->
                _isSyncEnabled.value = monitoring
                Log.d("BackgroundSleepVM", "Monitoreo cambió: $monitoring")
            }
        }
    }
    
    private fun observeSleepState() {
        viewModelScope.launch {
            while (!serviceManager.isServiceBound()) {
                kotlinx.coroutines.delay(100)
            }
            
            serviceManager.getCurrentSleepState()?.collect { sleepState ->
                _currentSleepState.value = sleepState
                Log.d("BackgroundSleepVM", "Estado de sueño cambió: $sleepState")
            }
        }
    }
    
    fun startBackgroundSync(userId: String, userName: String) {
        Log.d("BackgroundSleepVM", "Iniciando sincronización en segundo plano para: $userName")
        
        currentUserId = userId
        currentUserName = userName
        
        // Asegurar que el servicio esté bound antes de iniciar
        serviceManager.bindToService()
        
        serviceManager.startMonitoring(userId, userName)
        _isSyncEnabled.value = true
        
        // Forzar reconexión para asegurar que todo esté actualizado
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Dar tiempo para que el servicio se inicie
            serviceManager.forceReconnect()
            reObserveServiceStates()
        }
    }
    
    fun stopBackgroundSync() {
        Log.d("BackgroundSleepVM", "Deteniendo sincronización en segundo plano")
        
        serviceManager.stopMonitoring()
        _isSyncEnabled.value = false
        _isConnected.value = false
        _currentSleepState.value = null
        
        currentUserId = null
        currentUserName = null
    }
    
    fun sendSleepState(userId: String, userName: String, sleepState: SleepStateEnum) {
        if (!_isSyncEnabled.value) {
            Log.w("BackgroundSleepVM", "Sincronización no está habilitada")
            return
        }
        
        Log.d("BackgroundSleepVM", "Enviando estado de sueño: ${sleepState.displayName}")
        
        serviceManager.sendSleepState(userId, userName, sleepState)
        _currentSleepState.value = sleepState
    }
    
    fun toggleSync(userId: String, userName: String) {
        if (_isSyncEnabled.value) {
            stopBackgroundSync()
        } else {
            startBackgroundSync(userId, userName)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d("BackgroundSleepVM", "ViewModel limpiado")
        serviceManager.unbindFromService()
    }
}
