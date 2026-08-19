package com.example.appmobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.appmobile.presentation.ui.navigation.AppNavHost
import com.example.appmobile.presentation.shared.PhoneDataHolder
import com.example.appmobile.presentation.simulation.HeartRateSimulator
import com.example.appmobile.presentation.viewmodel.SignInViewModel
import com.example.appmobile.ui.theme.WereableAppTheme
import com.example.appmobile.domain.usecase.GoogleAuthUiClient
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmobile.presentation.viewmodel.SignInViewModelFactory
import com.google.android.gms.auth.api.identity.Identity

class MainActivity : ComponentActivity() {

    private lateinit var googleAuthUiClient: GoogleAuthUiClient

    private val wearDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra("type")) {
                "hr" -> {
                    val hr = intent.getFloatExtra("value", 0f)
                    Log.i("MainActivity", "📲 Broadcast recibido HR: $hr")
                    PhoneDataHolder.heartRate.value = hr
                }
                "hrv" -> {
                    val hrv = intent.getStringExtra("value")
                    Log.i("MainActivity", "📲 Broadcast recibido HRV: $hrv")
                    PhoneDataHolder.hrv.value = hrv
                }
                "phase" -> {
                    val phase = intent.getStringExtra("value")
                    Log.i("MainActivity", "📲 Broadcast recibido SleepPhase: $phase")
                    PhoneDataHolder.sleepPhase.value = phase
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Aplicación iniciada")

        // Inicializar GoogleAuthUiClient manualmente
        googleAuthUiClient = GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        HeartRateSimulator.startSimulation(lifecycleScope)

        enableEdgeToEdge()

        setContent {
            WereableAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Crear el ViewModel aquí para poder usarlo en el launcher
                    val signInViewModel: SignInViewModel = viewModel(
                        factory = SignInViewModelFactory(googleAuthUiClient, applicationContext)
                    )

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                        onResult = { result ->
                            if (result.resultCode == RESULT_OK) {
                                lifecycleScope.launch {
                                    val signInResult = googleAuthUiClient.signInWithIntent(
                                        result.data ?: return@launch
                                    )
                                    signInViewModel.onSignInResult(signInResult)
                                }
                            }
                        }
                    )

                    AppNavHost(
                        navController = navController,
                        googleAuthUiClient = googleAuthUiClient,
                        signInViewModel = signInViewModel,
                        launcher = launcher,
                        onSignInClick = {
                            signInViewModel.signIn()
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            wearDataReceiver,
            IntentFilter("com.example.ACTION_WEAR_DATA"),
            RECEIVER_NOT_EXPORTED
        )
        Log.i("MainActivity", "✅ BroadcastReceiver registrado")
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(wearDataReceiver)
        Log.i("MainActivity", "❌ BroadcastReceiver desregistrado")
    }
}
