package com.example.dashboardapp.presentation.ui.screens.register

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dashboardapp.R
import com.example.dashboardapp.presentation.ui.components.animates.AnimatedBackground
import com.example.dashboardapp.presentation.viewmodel.auth.RegisterViewModel
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

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

    AnimatedBackground {
        val state by viewModel.uiState.collectAsState()
        val context = LocalContext.current

        var userName by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var mobilePhone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        val role = "SysAdmin"
        val phoneOffice = ""
        val phoneExt = ""
        val roles = listOf(role)
        val active = true

        LaunchedEffect(state.isSuccess) {
            if (state.isSuccess) {
                Toast.makeText(context, "Registro y login exitoso", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
        }

        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->

            val configuration = LocalConfiguration.current
            val isTablet = configuration.screenWidthDp >= 600

            val columnModifier = if (isTablet) {
                Modifier
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 24.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = columnModifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "App icon",
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        text = "NOX",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(32.dp))


                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Nombre de usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apellido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val desc =
                                if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = desc)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mobilePhone,
                        onValueChange = { mobilePhone = it },
                        label = { Text("Teléfono móvil") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val trimmedUserName = userName.trim()
                            val trimmedFirstName = firstName.trim()
                            val trimmedLastName = lastName.trim()
                            val trimmedPassword = password.trim()
                            val trimmedMobilePhone = mobilePhone.trim()
                            val trimmedEmail = email.trim()

                            when {
                                trimmedUserName.length !in 4..30 ->
                                    showToast(
                                        context,
                                        "El nombre de usuario debe tener entre 4 y 30 caracteres"
                                    )

                                trimmedFirstName.length !in 4..30 ->
                                    showToast(
                                        context,
                                        "El nombre debe tener entre 4 y 30 caracteres"
                                    )

                                trimmedLastName.length !in 4..30 ->
                                    showToast(
                                        context,
                                        "El apellido debe tener entre 4 y 30 caracteres"
                                    )

                                trimmedPassword.length < 4 ->
                                    showToast(
                                        context,
                                        "La contraseña debe tener al menos 4 caracteres"
                                    )

                                trimmedMobilePhone.length != 10 ->
                                    showToast(context, "El número telefónico debe tener 10 dígitos")

                                trimmedEmail.length !in 4..60 ->
                                    showToast(context, "El correo electrónico no es válido")

                                else -> viewModel.register(
                                    trimmedUserName,
                                    trimmedFirstName,
                                    trimmedLastName,
                                    trimmedPassword,
                                    roles,
                                    trimmedMobilePhone,
                                    phoneOffice,
                                    phoneExt,
                                    trimmedEmail,
                                    active,
                                    role
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Registrarse")
                        }
                    }

                    state.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? Inicia sesión"
                        )
                    }
                }
            }

        }
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
