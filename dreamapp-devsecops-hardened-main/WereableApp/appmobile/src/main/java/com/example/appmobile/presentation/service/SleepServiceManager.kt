package com.example.appmobile.presentation.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.appmobile.presentation.websocket.SleepStateEnum
import kotlinx.coroutines.flow.StateFlow

class SleepServiceManager(private val context: Context) {
    
    private var service: SleepMonitoringService? = null
    private var bound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Log.d("SleepServiceManager", "Servicio conectado")
            val sleepBinder = binder as SleepMonitoringService.SleepMonitoringBinder
            service = sleepBinder.getService()
            bound = true
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("SleepServiceManager", "Servicio desconectado")
            bound = false
            service = null
        }
    }
    
    fun startMonitoring(userId: String, userName: String) {
        Log.d("SleepServiceManager", "Iniciando monitoreo para: $userName")
        
        val intent = Intent(context, SleepMonitoringService::class.java).apply {
            action = SleepMonitoringService.ACTION_START_MONITORING
            putExtra(SleepMonitoringService.EXTRA_USER_ID, userId)
            putExtra(SleepMonitoringService.EXTRA_USER_NAME, userName)
        }
        
        // Iniciar como servicio en primer plano
        context.startForegroundService(intent)
        
        // Bind al servicio para comunicación
        // Usar un pequeño delay para asegurar que el servicio esté iniciado
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            bindToService()
        }, 500) // 500ms delay
    }
    
    fun stopMonitoring() {
        Log.d("SleepServiceManager", "Deteniendo monitoreo")
        
        val intent = Intent(context, SleepMonitoringService::class.java).apply {
            action = SleepMonitoringService.ACTION_STOP_MONITORING
        }
        
        context.startService(intent)
        unbindFromService()
    }
    
    fun sendSleepState(userId: String, userName: String, sleepState: SleepStateEnum) {
        if (bound && service != null) {
            // Usar el servicio directamente si está bound
            service?.sendSleepState(userId, userName, sleepState)
        } else {
            // Usar Intent si no está bound
            val intent = Intent(context, SleepMonitoringService::class.java).apply {
                action = SleepMonitoringService.ACTION_SEND_SLEEP_STATE
                putExtra(SleepMonitoringService.EXTRA_USER_ID, userId)
                putExtra(SleepMonitoringService.EXTRA_USER_NAME, userName)
                putExtra(SleepMonitoringService.EXTRA_SLEEP_STATE, sleepState.name)
            }
            context.startService(intent)
        }
    }
    
    fun bindToService() {
        if (!bound) {
            Log.d("SleepServiceManager", "Binding al servicio...")
            val intent = Intent(context, SleepMonitoringService::class.java)
            val success = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d("SleepServiceManager", "Bind result: $success")
        } else {
            Log.d("SleepServiceManager", "Ya está bound al servicio")
        }
    }
    
    fun unbindFromService() {
        if (bound) {
            Log.d("SleepServiceManager", "Unbinding del servicio...")
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e("SleepServiceManager", "Error al unbind: ${e.message}")
            }
            bound = false
            service = null
        }
    }
    
    // Estados observables del servicio
    fun getConnectionState(): StateFlow<Boolean>? = service?.isConnected
    fun getMonitoringState(): StateFlow<Boolean>? = service?.isMonitoring
    fun getCurrentSleepState(): StateFlow<SleepStateEnum?>? = service?.currentSleepState
    
    fun isServiceBound(): Boolean = bound
    fun getService(): SleepMonitoringService? = service
    
    // Función para forzar reconexión
    fun forceReconnect() {
        Log.d("SleepServiceManager", "Forzando reconexión...")
        if (bound && service != null) {
            Log.d("SleepServiceManager", "Servicio disponible - Estados: Monitoring=${service?.isMonitoring?.value}, Connected=${service?.isConnected?.value}")
        } else {
            Log.d("SleepServiceManager", "Servicio NO disponible - Bound: $bound")
            bindToService()
        }
    }
}
