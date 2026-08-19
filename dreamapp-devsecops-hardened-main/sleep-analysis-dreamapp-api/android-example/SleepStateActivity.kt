// SleepStateActivity.kt
// Ejemplo de Activity que usa el WebSocket para enviar estados de sueño

package com.example.dreamapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dreamapp.websocket.LocationData
import com.example.dreamapp.websocket.SleepStateEnum
import com.example.dreamapp.websocket.SleepStateWebSocketClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson

class SleepStateActivity : AppCompatActivity() {
    
    private lateinit var webSocketClient: SleepStateWebSocketClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var statusTextView: TextView
    private lateinit var btnRem: Button
    private lateinit var btnLight: Button
    private lateinit var btnDeep: Button
    private lateinit var btnAwake: Button
    
    // Estos valores deberían venir de tu sistema de autenticación
    private val userId = "user_${System.currentTimeMillis()}" // Generar ID único
    private val userName = "Usuario Móvil" // Obtener del perfil del usuario
    private val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    
    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val WEBSOCKET_URL = "ws://tu-servidor.com:7070/ws/sleep/mobile" // Cambiar por tu URL
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_state)
        
        initializeViews()
        initializeWebSocket()
        initializeLocation()
        setupClickListeners()
        
        // Conectar automáticamente
        webSocketClient.connect()
    }
    
    private fun initializeViews() {
        statusTextView = findViewById(R.id.tvConnectionStatus)
        btnRem = findViewById(R.id.btnRem)
        btnLight = findViewById(R.id.btnLight)
        btnDeep = findViewById(R.id.btnDeep)
        btnAwake = findViewById(R.id.btnAwake)
    }
    
    private fun initializeWebSocket() {
        webSocketClient = SleepStateWebSocketClient(
            serverUrl = WEBSOCKET_URL,
            onConnectionChanged = { isConnected ->
                runOnUiThread {
                    updateConnectionStatus(isConnected)
                    updateButtonsState(isConnected)
                }
            },
            onMessageReceived = { message ->
                runOnUiThread {
                    handleServerMessage(message)
                }
            }
        )
    }
    
    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    
    private fun setupClickListeners() {
        btnRem.setOnClickListener { sendSleepState(SleepStateEnum.REM) }
        btnLight.setOnClickListener { sendSleepState(SleepStateEnum.LIGHT) }
        btnDeep.setOnClickListener { sendSleepState(SleepStateEnum.DEEP) }
        btnAwake.setOnClickListener { sendSleepState(SleepStateEnum.AWAKE) }
    }
    
    private fun updateConnectionStatus(isConnected: Boolean) {
        statusTextView.text = if (isConnected) {
            "🟢 Conectado al servidor"
        } else {
            "🔴 Desconectado del servidor"
        }
        statusTextView.setTextColor(
            ContextCompat.getColor(
                this, 
                if (isConnected) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            )
        )
    }
    
    private fun updateButtonsState(isConnected: Boolean) {
        btnRem.isEnabled = isConnected
        btnLight.isEnabled = isConnected
        btnDeep.isEnabled = isConnected
        btnAwake.isEnabled = isConnected
    }
    
    private fun sendSleepState(sleepState: SleepStateEnum) {
        if (!webSocketClient.isConnected()) {
            Toast.makeText(this, "No hay conexión con el servidor", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Cambiar color del botón temporalmente para feedback visual
        val button = when (sleepState) {
            SleepStateEnum.REM -> btnRem
            SleepStateEnum.LIGHT -> btnLight
            SleepStateEnum.DEEP -> btnDeep
            SleepStateEnum.AWAKE -> btnAwake
        }
        
        val originalColor = button.backgroundTintList
        button.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_light)
        
        // Restaurar color después de 1 segundo
        button.postDelayed({
            button.backgroundTintList = originalColor
        }, 1000)
        
        // Obtener ubicación y enviar estado
        getCurrentLocationAndSend(sleepState)
    }
    
    private fun getCurrentLocationAndSend(sleepState: SleepStateEnum) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            
            // Enviar sin ubicación si no hay permisos
            webSocketClient.sendSleepState(
                userId = userId,
                userName = userName,
                sleepState = sleepState,
                deviceId = deviceId
            )
            
            // Solicitar permisos para próximas veces
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                val locationData = location?.let {
                    LocationData(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        address = "Ubicación del dispositivo"
                    )
                }
                
                webSocketClient.sendSleepState(
                    userId = userId,
                    userName = userName,
                    sleepState = sleepState,
                    deviceId = deviceId,
                    location = locationData
                )
                
                Toast.makeText(
                    this, 
                    "Estado ${sleepState.displayName} enviado", 
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                // Enviar sin ubicación si hay error
                webSocketClient.sendSleepState(
                    userId = userId,
                    userName = userName,
                    sleepState = sleepState,
                    deviceId = deviceId
                )
                
                Toast.makeText(
                    this, 
                    "Estado ${sleepState.displayName} enviado (sin ubicación)", 
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    
    private fun handleServerMessage(message: String) {
        try {
            val gson = Gson()
            val response = gson.fromJson(message, Map::class.java)
            
            when (response["status"]) {
                "success" -> {
                    val sleepState = response["sleepState"] as? String
                    Toast.makeText(
                        this, 
                        "✅ Estado $sleepState confirmado por el servidor", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                "error" -> {
                    val errorMsg = response["message"] as? String ?: "Error desconocido"
                    Toast.makeText(this, "❌ Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error procesando respuesta del servidor", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permisos de ubicación concedidos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Sin permisos de ubicación, se enviará sin coordenadas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.disconnect()
    }
    
    override fun onPause() {
        super.onPause()
        // Opcional: desconectar cuando la app pasa a segundo plano
        // webSocketClient.disconnect()
    }
    
    override fun onResume() {
        super.onResume()
        // Reconectar si no está conectado
        if (!webSocketClient.isConnected()) {
            webSocketClient.connect()
        }
    }
}
