package com.example.appmobile.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.appmobile.R
import com.example.appmobile.MainActivity
import com.example.appmobile.presentation.websocket.SleepStateEnum
import com.example.appmobile.presentation.websocket.SleepStateWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepMonitoringService : Service() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "sleep_monitoring_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
        const val ACTION_SEND_SLEEP_STATE = "SEND_SLEEP_STATE"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_NAME = "user_name"
        const val EXTRA_SLEEP_STATE = "sleep_state"
    }
    
    private val binder = SleepMonitoringBinder()
    private var webSocketClient: SleepStateWebSocketClient? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Estados observables
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private val _currentSleepState = MutableStateFlow<SleepStateEnum?>(null)
    val currentSleepState: StateFlow<SleepStateEnum?> = _currentSleepState.asStateFlow()
    
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    
    // URL del servidor
    private val serverUrl = "${com.example.appmobile.BuildConfig.WS_BASE_URL}/ws/sleep/mobile"
    
    inner class SleepMonitoringBinder : Binder() {
        fun getService(): SleepMonitoringService = this@SleepMonitoringService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        Log.d("SleepService", "Servicio de monitoreo creado")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                val userName = intent.getStringExtra(EXTRA_USER_NAME)
                if (userId != null && userName != null) {
                    startMonitoring(userId, userName)
                }
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoring()
            }
            ACTION_SEND_SLEEP_STATE -> {
                val userId = intent.getStringExtra(EXTRA_USER_ID)
                val userName = intent.getStringExtra(EXTRA_USER_NAME)
                val sleepStateStr = intent.getStringExtra(EXTRA_SLEEP_STATE)
                
                if (userId != null && userName != null && sleepStateStr != null) {
                    try {
                        val sleepState = SleepStateEnum.valueOf(sleepStateStr)
                        sendSleepState(userId, userName, sleepState)
                    } catch (e: Exception) {
                        Log.e("SleepService", "Error parsing sleep state: $sleepStateStr", e)
                    }
                }
            }
        }
        
        return START_STICKY // Reiniciar si el sistema mata el servicio
    }
    
    private fun startMonitoring(userId: String, userName: String) {
        Log.d("SleepService", "Iniciando monitoreo para usuario: $userName")
        
        // Si ya hay un cliente activo, desconectarlo primero
        webSocketClient?.disconnect()
        
        currentUserId = userId
        currentUserName = userName
        
        // Crear conexión WebSocket
        webSocketClient = SleepStateWebSocketClient(
            serverUrl = serverUrl,
            onConnectionChanged = { connected ->
                Log.d("SleepService", "Estado de conexión cambió: $connected")
                _isConnected.value = connected
                updateNotification()
            },
            onMessageReceived = { message ->
                Log.d("SleepService", "Mensaje recibido: $message")
            }
        )
        
        webSocketClient?.connect()
        _isMonitoring.value = true
        
        // Iniciar como servicio en primer plano
        startForeground(NOTIFICATION_ID, createNotification())
        
        Log.d("SleepService", "Monitoreo iniciado - Monitoring: ${_isMonitoring.value}, Connected: ${_isConnected.value}")
    }
    
    fun stopMonitoring() {
        Log.d("SleepService", "Deteniendo monitoreo")
        
        // Enviar mensaje de desconexión
        if (_isConnected.value && currentUserId != null && currentUserName != null) {
            sendDisconnectionMessage(currentUserId!!, currentUserName!!)
        }
        
        webSocketClient?.disconnect()
        webSocketClient = null
        
        _isMonitoring.value = false
        _isConnected.value = false
        _currentSleepState.value = null
        
        currentUserId = null
        currentUserName = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun sendSleepState(userId: String, userName: String, sleepState: SleepStateEnum) {
        if (!_isConnected.value) {
            Log.w("SleepService", "No hay conexión activa")
            return
        }
        
        currentUserId = userId
        currentUserName = userName
        
        webSocketClient?.sendSleepState(
            userId = userId,
            userName = userName,
            sleepState = sleepState,
            deviceId = "android_service_${System.currentTimeMillis()}"
        )
        
        _currentSleepState.value = sleepState
        updateNotification()
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
            Log.d("SleepService", "Enviado mensaje de desconexión para usuario: $userName")
        } catch (e: Exception) {
            Log.e("SleepService", "Error enviando mensaje de desconexión", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Monitoreo de Sueño",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones para el monitoreo continuo de sueño"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, SleepMonitoringService::class.java).apply {
            action = ACTION_STOP_MONITORING
        }
        
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val statusText = when {
            !_isMonitoring.value -> "Monitoreo detenido"
            !_isConnected.value -> "Conectando..."
            _currentSleepState.value != null -> "Estado: ${_currentSleepState.value?.displayName}"
            else -> "Esperando datos..."
        }
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("🌙 Monitoreo de Sueño Activo")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_notification) // Necesitarás crear este icono
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Detener", stopPendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    private fun updateNotification() {
        if (_isMonitoring.value) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("SleepService", "Servicio destruido")
        
        webSocketClient?.disconnect()
        serviceScope.cancel()
    }
}
