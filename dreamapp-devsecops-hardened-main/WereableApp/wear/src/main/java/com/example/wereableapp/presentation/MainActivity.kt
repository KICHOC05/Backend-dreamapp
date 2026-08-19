package com.example.wereableapp.presentation

import SleepMonitorScreen
import android.content.Intent
import android.os.Build
import com.example.wereableapp.presentation.service.SensorForegroundService
import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.wereableapp.presentation.data.communication.WearConnectionImpl
import com.example.wereableapp.presentation.data.repository.SensorRepositoryImpl
import com.example.wereableapp.presentation.data.repository.SleepPhaseRepositoryImpl
import com.example.wereableapp.presentation.presentation.viewmodel.SleepMonitorViewModel
import com.example.wereableapp.presentation.theme.WereableAppTheme
import android.content.pm.PackageManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: SleepMonitorViewModel

    // Nuevo: registro del launcher para pedir permiso
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("MainActivity", "✅ Permiso BODY_SENSORS concedido")
            startApp()
        } else {
            Log.e("MainActivity", "❌ Permiso BODY_SENSORS denegado. No se puede leer el sensor.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasSensorPermission()) {
            startApp()
        } else {
            permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
        // Inicia el servicio en segundo plano
        val serviceIntent = Intent(this, SensorForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, SensorForegroundService::class.java))
        } else {
            startService(Intent(this, SensorForegroundService::class.java))
        }
        // Esto asegura la conexión al DataClient o MessageClient
        Wearable.getMessageClient(this).addListener(object : MessageClient.OnMessageReceivedListener {
            override fun onMessageReceived(event: MessageEvent) {
                Log.d("MainActivity", "🔔 Mensaje recibido desde wearable: ${event.path}")
            }
        })
    }

    private fun hasSensorPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startApp() {
        val sensorRepository = SensorRepositoryImpl(this)
        val wearConnectionRepository = WearConnectionImpl(this)
        val sleepPhaseRepository = SleepPhaseRepositoryImpl(this)

        viewModel = SleepMonitorViewModel(
            sensorRepository,
            wearConnectionRepository,
            sleepPhaseRepository
        )

        setContent {
            WereableAppTheme {
                SleepMonitorScreen(viewModel)
            }
        }
    }
}
