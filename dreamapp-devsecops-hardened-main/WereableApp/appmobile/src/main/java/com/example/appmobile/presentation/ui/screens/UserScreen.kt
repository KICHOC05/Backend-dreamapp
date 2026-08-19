package com.example.appmobile.presentation.ui.screens

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.appmobile.R
import com.example.appmobile.presentation.ui.components.animates.AnimatedBackground
import com.example.appmobile.presentation.ui.user.UserForm
import com.example.appmobile.presentation.viewmodel.RegisterViewModel
import com.example.appmobile.presentation.viewmodel.RegisterViewModelFactory
import com.example.appmobile.presentation.viewmodel.SignInViewModel

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    onUserRegistered: () -> Unit,
    uid: String? = null  // Nuevo parámetro para recibir el UID
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    
    // ViewModel con factory que incluye el contexto
    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(context)
    )

    // MediaPlayer con música de fondo
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.song1).apply {
            isLooping = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!mediaPlayer.isPlaying) mediaPlayer.start()
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    if (mediaPlayer.isPlaying) mediaPlayer.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    mediaPlayer.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mediaPlayer.release()
        }
    }

    // Estado del registro
    val registerState by registerViewModel.uiState.collectAsState()

    LaunchedEffect(registerState.isSuccess) {
        if (registerState.isSuccess) {
            Toast.makeText(context, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
            onUserRegistered()
        }
    }

    LaunchedEffect(registerState.error) {
        registerState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            registerViewModel.resetState()
        }
    }

    AnimatedBackground {
        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (registerState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    UserForm(
                        onSubmit = { userData ->
                            if (uid.isNullOrBlank()) {
                                Toast.makeText(
                                    context,
                                    "Sesión inválida. Por favor inicia sesión de nuevo.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@UserForm
                            }
                            
                            registerViewModel.registerUser(
                                uidUser = uid,
                                weightKg = userData.peso.toDouble(),
                                heightCm = userData.estatura.toDouble(),
                                age = userData.edad,
                                sex = userData.sexo
                            )
                        }
                    )
                }
            }
        }
    }
}