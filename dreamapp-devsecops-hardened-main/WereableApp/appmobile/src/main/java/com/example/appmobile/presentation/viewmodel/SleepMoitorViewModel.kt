package com.example.appmobile.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.presentation.shared.PhoneDataHolder
import com.example.appmobile.data.database.SleepDatabase
import com.example.appmobile.data.database.entity.SleepCycleEntity
import com.example.appmobile.data.repository.SleepDataRepository
import com.example.appmobile.data.database.entity.SleepDataEntity
import com.example.appmobile.data.database.entity.SleepPhaseDataEntity
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SleepMonitorViewModel(application: Application) : AndroidViewModel(application)
 {

    private val nodeClient: NodeClient = Wearable.getNodeClient(application)
    private val _connectedNodeName = MutableStateFlow<String>("Desconocido")
    val connectedNodeName: StateFlow<String> = _connectedNodeName

    // Database setup
    private val database = SleepDatabase.getDatabase(application)
    private val repository = SleepDataRepository(database.sleepDataDao())
    
    // Flow para los últimos 20 registros
    val last20Records: StateFlow<List<SleepDataEntity>> = 
        repository.getLast20Records().let { flow ->
            val stateFlow = MutableStateFlow<List<SleepDataEntity>>(emptyList())
            viewModelScope.launch {
                flow.collect { records ->
                    stateFlow.value = records
                }
            }
            stateFlow
        }

    // Agregar contadores de diagnóstico
    private val _messagesReceived = MutableStateFlow(0)
    val messagesReceived: StateFlow<Int> = _messagesReceived

    private val _lastMessageTime = MutableStateFlow<String?>(null)
    val lastMessageTime: StateFlow<String?> = _lastMessageTime

    fun fetchConnectedNodeName() {
        viewModelScope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                Log.d("SleepMonitorVM", "🔍 Nodos conectados: ${nodes.size}")
                
                if (nodes.isNotEmpty()) {
                    val node = nodes.first()
                    _connectedNodeName.value = node.displayName
                    Log.i("SleepMonitorVM", "📱 Conectado a: ${node.displayName} (${node.id})")
                } else {
                    _connectedNodeName.value = "No hay dispositivos conectados"
                    Log.w("SleepMonitorVM", "⚠️ No hay dispositivos conectados")
                }
            } catch (e: Exception) {
                _connectedNodeName.value = "Error: ${e.message}"
                Log.e("SleepMonitorVM", "❌ Error obteniendo nodos: ${e.message}")
            }
        }
    }

    val heartRate: StateFlow<Float?> = PhoneDataHolder.heartRate
    val hrv: StateFlow<String?> = PhoneDataHolder.hrv
    val sleepPhase: StateFlow<String?> = PhoneDataHolder.sleepPhase

    // Nuevo: Estado de monitoreo
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring

    fun startMonitoring() {
        _isMonitoring.value = true
        Log.i("SleepMonitorVM", "🟢 Monitoreo iniciado")
        
        // Iniciar seguimiento de cambios en los datos y guardar en BD
        viewModelScope.launch {
            PhoneDataHolder.heartRate.collect { hr ->
                if (hr != null) {
                    _messagesReceived.value = _messagesReceived.value + 1
                    _lastMessageTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    Log.i("SleepMonitorVM", "📊 HR actualizado: $hr (msgs: ${_messagesReceived.value})")
                    
                    // Guardar en BD cuando recibimos datos
                    saveCurrentDataToDatabase()
                }
            }
        }
        
        viewModelScope.launch {
            PhoneDataHolder.hrv.collect { hrv ->
                if (hrv != null) {
                    _messagesReceived.value = _messagesReceived.value + 1
                    _lastMessageTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    Log.i("SleepMonitorVM", "📊 HRV actualizado: $hrv (msgs: ${_messagesReceived.value})")
                    
                    // Guardar en BD cuando recibimos datos
                    saveCurrentDataToDatabase()
                }
            }
        }
        
        viewModelScope.launch {
            PhoneDataHolder.sleepPhase.collect { phase ->
                if (phase != null) {
                    _messagesReceived.value = _messagesReceived.value + 1
                    _lastMessageTime.value = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    Log.i("SleepMonitorVM", "📊 Phase actualizado: $phase (msgs: ${_messagesReceived.value})")
                    
                    // Guardar en BD cuando recibimos datos
                    saveCurrentDataToDatabase()
                }
            }
        }
    }
    
    private fun saveCurrentDataToDatabase() {
        viewModelScope.launch {
            try {
                val currentHR = PhoneDataHolder.heartRate.value
                val currentHRV = PhoneDataHolder.hrv.value
                val currentPhase = PhoneDataHolder.sleepPhase.value
                
                // Solo guardar si tenemos al menos un dato
                if (currentHR != null || currentHRV != null || currentPhase != null) {
                    repository.insertSleepData(currentHR, currentHRV, currentPhase)
                    Log.i("SleepMonitorVM", "💾 Datos guardados en BD: HR=$currentHR, HRV=$currentHRV, Phase=$currentPhase")
                }
            } catch (e: Exception) {
                Log.e("SleepMonitorVM", "❌ Error guardando en BD: ${e.message}")
            }
        }
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        Log.i("SleepMonitorVM", "🔴 Monitoreo detenido")
    }
    
    // Función para limpiar datos (útil para testing)
    fun clearTestData() {
        viewModelScope.launch {
            PhoneDataHolder.heartRate.value = null
            PhoneDataHolder.hrv.value = null
            PhoneDataHolder.sleepPhase.value = null
            _messagesReceived.value = 0
            _lastMessageTime.value = null
            Log.i("SleepMonitorVM", "🧹 Datos limpiados")
        }
    }
     private val db = SleepDatabase.getDatabase(application)
     private val sleepCycleDao = db.sleepCycleDao()
     private val sleepPhaseDataDao = db.sleepPhaseDataDao()

     val sleepCycles: StateFlow<List<SleepCycleEntity>> = sleepCycleDao
         .getAllCycles()
         .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

     private val _selectedCycleId = MutableStateFlow<Long?>(null)

     val sleepPhases: StateFlow<List<SleepPhaseDataEntity>> = _selectedCycleId
         .filterNotNull()
         .flatMapLatest { cycleId ->
             sleepPhaseDataDao.getPhasesForCycle(cycleId)
         }
         .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

     fun selectCycle(cycleId: Long) {
         _selectedCycleId.value = cycleId
     }
}