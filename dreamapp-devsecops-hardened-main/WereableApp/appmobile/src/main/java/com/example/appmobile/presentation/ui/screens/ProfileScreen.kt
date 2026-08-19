package com.example.appmobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appmobile.data.repository.HeartRateRepository
import com.example.appmobile.data.repository.SleepDataType
import com.example.appmobile.domain.model.UserData
import com.example.appmobile.presentation.viewmodel.BackgroundSleepViewModel
import com.example.appmobile.presentation.viewmodel.SleepUploadViewModel
import com.example.appmobile.presentation.viewmodel.UploadResult
import com.example.appmobile.presentation.websocket.SleepStateEnum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
    onStartMonitoring: () -> Unit,
    onNavigateToUserScreen: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val bpmState = HeartRateRepository.bpm.collectAsState()
    
    // Background Sleep ViewModel
    val backgroundSleepViewModel: BackgroundSleepViewModel = viewModel()
    
    // Sleep Upload ViewModel
    val sleepUploadViewModel: SleepUploadViewModel = viewModel()
    
    val isConnected by backgroundSleepViewModel.isConnected.collectAsState()
    val isSyncEnabled by backgroundSleepViewModel.isSyncEnabled.collectAsState()
    val currentSleepState by backgroundSleepViewModel.currentSleepState.collectAsState()
    
    // Estados de subida
    val isUploading by sleepUploadViewModel.isUploading.collectAsState()
    val uploadResult by sleepUploadViewModel.uploadResult.collectAsState()
    val lastUploadMessage by sleepUploadViewModel.lastUploadMessage.collectAsState()
    
    // Verificar disponibilidad de fecha de hoy para todos los botones
    val canUploadToday = userData?.let { 
        sleepUploadViewModel.canUploadForDate(it.userId, SleepDataType.TODAY) 
    } ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                expanded = false
                                showLogoutDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Cerrar sesión"
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil
            item {
                if (!userData?.profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = userData?.profilePictureUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sin foto",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = userData?.username ?: "Usuario",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Funciones Principales",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = onStartMonitoring,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monitoreo de Sueño",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Historial de Sueño",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sincronización de Sueño",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = if (isConnected) 
                                                MaterialTheme.colorScheme.tertiary 
                                            else 
                                                MaterialTheme.colorScheme.error,
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = if (isConnected) "Conectado" else "Desconectado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { 
                                userData?.let { user ->
                                    if (user.username?.isNotEmpty() == true) {
                                        backgroundSleepViewModel.toggleSync(
                                            userId = user.userId,
                                            userName = user.username ?: "Usuario"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSyncEnabled)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isSyncEnabled) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSyncEnabled) "Desactivar Sincronización" else "Activar Sincronización",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (isSyncEnabled && isConnected) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )

                            Text(
                                text = "Fases del Sueño",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            currentSleepState?.let { state ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = getSleepStateIcon(state),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Estado actual: ${state.displayName}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SleepStateButton(
                                        state = SleepStateEnum.AWAKE,
                                        isSelected = currentSleepState == SleepStateEnum.AWAKE,
                                        onClick = {
                                            userData?.let { user ->
                                                if (user.username?.isNotEmpty() == true) {
                                                    backgroundSleepViewModel.sendSleepState(
                                                        userId = user.userId,
                                                        userName = user.username ?: "Usuario",
                                                        sleepState = SleepStateEnum.AWAKE
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    SleepStateButton(
                                        state = SleepStateEnum.LIGHT,
                                        isSelected = currentSleepState == SleepStateEnum.LIGHT,
                                        onClick = {
                                            userData?.let { user ->
                                                if (user.username?.isNotEmpty() == true) {
                                                    backgroundSleepViewModel.sendSleepState(
                                                        userId = user.userId,
                                                        userName = user.username ?: "Usuario",
                                                        sleepState = SleepStateEnum.LIGHT
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SleepStateButton(
                                        state = SleepStateEnum.DEEP,
                                        isSelected = currentSleepState == SleepStateEnum.DEEP,
                                        onClick = {
                                            userData?.let { user ->
                                                if (user.username?.isNotEmpty() == true) {
                                                    backgroundSleepViewModel.sendSleepState(
                                                        userId = user.userId,
                                                        userName = user.username ?: "Usuario",
                                                        sleepState = SleepStateEnum.DEEP
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    SleepStateButton(
                                        state = SleepStateEnum.REM,
                                        isSelected = currentSleepState == SleepStateEnum.REM,
                                        onClick = {
                                            userData?.let { user ->
                                                if (user.username?.isNotEmpty() == true) {
                                                    backgroundSleepViewModel.sendSleepState(
                                                        userId = user.userId,
                                                        userName = user.username ?: "Usuario",
                                                        sleepState = SleepStateEnum.REM
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else if (isSyncEnabled && !isConnected) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = "Conectando al servidor...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sección de subida de datos a la nube
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subir Datos a la Nube",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Sube datos de sueño simulados a Firebase Cloud Functions. Solo se permite una sesión por fecha específica.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (lastUploadMessage.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (uploadResult is UploadResult.Success)
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                    else
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = lastUploadMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uploadResult is UploadResult.Success)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón único: Datos de Hoy
                            Button(
                                onClick = {
                                    userData?.let { user ->
                                        sleepUploadViewModel.uploadSleepData(
                                            userId = user.userId,
                                            dataType = SleepDataType.TODAY
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isUploading && userData != null && canUploadToday,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canUploadToday) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outline
                                )
                            ) {
                                if (isUploading) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canUploadToday) 
                                        "Subir Datos de Hoy" 
                                    else 
                                        "Ya enviado hoy",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (userData == null) {
                            Text(
                                text = "⚠️ Debes iniciar sesión para subir datos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Nueva sección: Casos de Uso por Calidad
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Casos de Uso por Calidad",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Simula datos específicos según la calidad del sueño (POOR, FAIR, GOOD, EXCELLENT)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        uploadResult?.let { result ->
                            val message = when (result) {
                                is UploadResult.Success -> "✅ Datos subidos exitosamente"
                                is UploadResult.Error -> "❌ Error: ${result.message}"
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (result) {
                                        is UploadResult.Success -> MaterialTheme.colorScheme.primaryContainer
                                        is UploadResult.Error -> MaterialTheme.colorScheme.errorContainer
                                    }
                                )
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when (result) {
                                        is UploadResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                                        is UploadResult.Error -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón 1: Calidad POBRE
                            OutlinedButton(
                                onClick = {
                                    userData?.let { user ->
                                        sleepUploadViewModel.uploadSleepData(
                                            userId = user.userId,
                                            dataType = SleepDataType.POOR_QUALITY
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isUploading && userData != null && canUploadToday,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    contentColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            ) {
                                if (isUploading) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canUploadToday)
                                        "Calidad POBRE (40-60% eficiencia)"
                                    else
                                        "Ya enviado hoy",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Botón 2: Calidad REGULAR
                            OutlinedButton(
                                onClick = {
                                    userData?.let { user ->
                                        sleepUploadViewModel.uploadSleepData(
                                            userId = user.userId,
                                            dataType = SleepDataType.FAIR_QUALITY
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isUploading && userData != null && canUploadToday,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    contentColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentNeutral,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canUploadToday)
                                        "Calidad REGULAR (60-75% eficiencia)"
                                    else
                                        "Ya enviado hoy",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Botón 3: Calidad BUENA
                            OutlinedButton(
                                onClick = {
                                    userData?.let { user ->
                                        sleepUploadViewModel.uploadSleepData(
                                            userId = user.userId,
                                            dataType = SleepDataType.GOOD_QUALITY
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isUploading && userData != null && canUploadToday,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    contentColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfied,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canUploadToday)
                                        "Calidad BUENA (75-90% eficiencia)"
                                    else
                                        "Ya enviado hoy",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Botón 4: Calidad EXCELENTE
                            OutlinedButton(
                                onClick = {
                                    userData?.let { user ->
                                        sleepUploadViewModel.uploadSleepData(
                                            userId = user.userId,
                                            dataType = SleepDataType.EXCELLENT_QUALITY
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isUploading && userData != null && canUploadToday,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                    contentColor = if (canUploadToday)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentVerySatisfied,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canUploadToday)
                                        "Calidad EXCELENTE (90-98% eficiencia)"
                                    else
                                        "Ya enviado hoy",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (userData == null) {
                            Text(
                                text = "⚠️ Debes iniciar sesión para subir datos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Limpiar resultado después de un tiempo
    LaunchedEffect(uploadResult) {
        if (uploadResult != null) {
            kotlinx.coroutines.delay(5000) // 5 segundos
            sleepUploadViewModel.clearResult()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { 
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Text(
                    text = "¿Estás seguro de que quieres cerrar sesión?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    }
                ) {
                    Text(
                        text = "Sí, cerrar sesión",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun SleepStateButton(
    state: SleepStateEnum,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sleepStateColor = getSleepStateColor(state)
    
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                sleepStateColor.copy(alpha = 0.2f)
            } else {
                sleepStateColor.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getSleepStateIcon(state),
                contentDescription = state.displayName,
                tint = sleepStateColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) sleepStateColor else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun getSleepStateColor(state: SleepStateEnum): Color {
    return when (state) {
        SleepStateEnum.AWAKE -> MaterialTheme.colorScheme.tertiary
        SleepStateEnum.LIGHT -> MaterialTheme.colorScheme.secondary
        SleepStateEnum.DEEP -> MaterialTheme.colorScheme.primary
        SleepStateEnum.REM -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun getSleepStateIcon(state: SleepStateEnum): ImageVector {
    return when (state) {
        SleepStateEnum.AWAKE -> Icons.Default.WbSunny
        SleepStateEnum.LIGHT -> Icons.Default.Nightlight
        SleepStateEnum.DEEP -> Icons.Default.Bedtime
        SleepStateEnum.REM -> Icons.Default.RemoveRedEye
    }
}
