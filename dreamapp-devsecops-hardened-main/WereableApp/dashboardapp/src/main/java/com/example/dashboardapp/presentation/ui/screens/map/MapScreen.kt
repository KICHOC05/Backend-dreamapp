package com.example.dashboardapp.presentation.ui.screens.map

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dashboardapp.presentation.viewmodel.SleepStateViewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.random.Random

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val sleepStateViewModel: SleepStateViewModel = hiltViewModel()
    val sleepStates by sleepStateViewModel.sleepStates.collectAsState()
    val isConnected by sleepStateViewModel.isConnected.collectAsState()
    val showConnectionError by sleepStateViewModel.showConnectionError.collectAsState()
    val connectionMessage by sleepStateViewModel.connectionMessage.collectAsState()
    
    val userLocations = remember { mutableMapOf<String, GeoPoint>() }
    var showConnectionSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "com.example.dashboardapp"
    }
    
    LaunchedEffect(showConnectionError, connectionMessage) {
        if (showConnectionError && connectionMessage.isNotEmpty()) {
            showConnectionSnackbar = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con información de conexión y estadísticas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected || !showConnectionError) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isConnected || !showConnectionError) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isConnected || !showConnectionError) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isConnected -> "Conexión activa - ${sleepStates.size} usuarios monitoreados"
                            showConnectionError -> "Sin conexión al servidor"
                            else -> "Reconectando..." 
                        },
                        color = if (isConnected || !showConnectionError) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (isConnected && sleepStates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Estadísticas por fase de sueño
                    val phaseStats = sleepStates.values.groupBy { it.sleepState }.mapValues { it.value.size }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(phaseStats.entries.toList()) { (phase, count) ->
                            val (color, icon, displayName) = when (phase) {
                                "REM" -> Triple("#FF6B6B", "🔴", "REM")
                                "LIGHT" -> Triple("#4ECDC4", "🟢", "Ligero")
                                "DEEP" -> Triple("#45B7D1", "🔵", "Profundo")
                                "AWAKE" -> Triple("#96CEB4", "🟡", "Despierto")
                                else -> Triple("#757575", "⚫", "Otro")
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(color)))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$displayName: $count",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mapa
        Box(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            
                            // Configurar el controlador del mapa
                            val mapController: IMapController = controller
                            mapController.setZoom(12.0)
                            // Centrar en una ubicación por defecto (Ciudad de México)
                            mapController.setCenter(GeoPoint(19.4326, -99.1332))
                            
                            // Configurar estilo del mapa
                            this.overlays.clear()
                        }
                    },
                    update = { mapView ->
                        // Limpiar marcadores existentes
                        mapView.overlays.clear()
                        
                        // Solo mostrar usuarios conectados
                        if (isConnected) {
                            sleepStates.values.forEach { sleepState ->
                                // Mantener la ubicación fija del usuario o asignar una nueva si es la primera vez
                                val userLocation = userLocations.getOrPut(sleepState.userId) {
                                    // Generar ubicación aleatoria para demostración SOLO la primera vez
                                    val baseLocations = listOf(
                                        GeoPoint(19.4326, -99.1332), // Centro histórico
                                        GeoPoint(19.4267, -99.1718), // Polanco
                                        GeoPoint(19.3629, -99.0890), // Aeropuerto
                                        GeoPoint(19.3910, -99.2837), // Santa Fe
                                        GeoPoint(19.3848, -99.1591), // Roma Norte
                                        GeoPoint(19.4194, -99.1721), // Condesa
                                    )
                                    
                                    val baseLocation = baseLocations.random()
                                    val randomLat = baseLocation.latitude + (Random.nextDouble() - 0.5) * 0.02
                                    val randomLon = baseLocation.longitude + (Random.nextDouble() - 0.5) * 0.02
                                    GeoPoint(randomLat, randomLon)
                                }
                                
                                val marker = Marker(mapView).apply {
                                    position = userLocation
                                    title = sleepState.userName
                                    snippet = "Estado: ${sleepState.sleepStateDisplay}\nÚltima actualización: ${sleepState.timestamp}"
                                    
                                    // Configurar color del marcador según el estado de sueño
                                    val markerColor = when (sleepState.sleepState) {
                                        "REM" -> "#FF6B6B"
                                        "LIGHT" -> "#4ECDC4"
                                        "DEEP" -> "#45B7D1"
                                        "AWAKE" -> "#96CEB4"
                                        else -> "#757575"
                                    }
                                    
                                    // Crear un marcador customizado con color
                                    icon = createColoredMarkerDrawable(context, markerColor)
                                    
                                    // Configurar la ventana de información
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                
                                mapView.overlays.add(marker)
                            }
                        }
                        
                        // Limpiar ubicaciones de usuarios que ya no están conectados
                        val currentUserIds = sleepStates.keys
                        userLocations.keys.removeAll { userId -> userId !in currentUserIds }
                        
                        mapView.invalidate()
                    }
                )
            }
            
            // Snackbar para notificaciones de conexión
            if (showConnectionSnackbar && showConnectionError) {
                LaunchedEffect(connectionMessage) {
                    kotlinx.coroutines.delay(5000)
                    showConnectionSnackbar = false
                }
                
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = connectionMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun createColoredMarkerDrawable(context: Context, colorHex: String): android.graphics.drawable.Drawable {
    val color = try {
        android.graphics.Color.parseColor(colorHex)
    } catch (e: Exception) {
        android.graphics.Color.GRAY
    }
    
    // Crear un drawable circular coloreado más atractivo
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setColor(color)
    drawable.setStroke(6, android.graphics.Color.WHITE) // Borde blanco más grueso
    drawable.setSize(60, 60) // Tamaño más grande para mejor visibilidad
    
    // Agregar sombra (efecto de elevación)
    drawable.gradientType = GradientDrawable.RADIAL_GRADIENT
    drawable.gradientRadius = 30f
    
    return drawable
}
