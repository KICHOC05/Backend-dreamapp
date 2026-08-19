package com.example.dashboardapp.presentation.ui.components.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dashboardapp.domain.model.sleep.StatsAndAveragesDomain
import com.example.dashboardapp.presentation.viewmodel.sleep.SleepStatsUiState

@Composable
fun CardStatsLastDay(uiState: SleepStatsUiState) {
    var statsMode by remember { mutableStateOf("lastDay") }
    val colort = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep Stats",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (statsMode == "lastDay") "Estadísticas del Último Día" else "Promedios de la Semana",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (statsMode == "lastDay") "Resumen del sueño" else "Promedios de los últimos 7 días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is SleepStatsUiState.Loading -> LoadingStatsContent()
                is SleepStatsUiState.Success -> {
                    val statsToShow = if (statsMode == "lastDay") {
                        uiState.data.statsLastDay
                    } else {
                        uiState.data.averagesLastWeek
                    }
                    StatsContent(statsToShow)
                }
                is SleepStatsUiState.Error -> ErrorStatsContent(uiState.message)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val buttonItems = listOf(
                    "lastDay" to "Último Día",
                    "averages" to "Promedios ultima semana"
                )

                buttonItems.forEachIndexed { idx, (mode, label) ->
                    val isSelected = statsMode == mode
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { statsMode = mode }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .drawBehind {
                                    if (isSelected) {
                                        val strokeWidth = 4.dp.toPx()
                                        drawLine(
                                            brush = SolidColor(colort),
                                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                }
                                .height(32.dp)
                        )
                    }
                    if (idx < buttonItems.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "|",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsContent(stats: StatsAndAveragesDomain) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                label = "Eficiencia",
                value = "${stats.sleepEfficiency}%",
                color = getEfficiencyColor(stats.sleepEfficiency)
            )
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Hotel,
                label = "Duración",
                value = "${stats.sleepDuration} min",
                color = getDynamicColor(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Visibility,
                label = "Sueño Ligero",
                value = "${stats.light} min",
                color = getDynamicColor(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
            )
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Bedtime,
                label = "Sueño Profundo",
                value = "${stats.deep} min",
                color = getDynamicColor(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsRun,
                label = "REM",
                value = "${stats.rem} min",
                color = getDynamicColor(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            )
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Visibility,
                label = "Despierto",
                value = "${stats.awake} min",
                color = getDynamicColor(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                label = "FC Promedio",
                value = "${stats.avgHR} bpm",
                color = getDynamicColor(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
            )
            StatItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                label = "Despertares",
                value = "${stats.awakenings}",
                color = getDynamicColor(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LoadingStatsContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cargando estadísticas...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ErrorStatsContent(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error al cargar datos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun getEfficiencyColor(efficiency: Int): Color {
    val darkTheme = isSystemInDarkTheme()
    return when {
        efficiency >= 85 -> if (darkTheme) Color(0xFF81C784) else Color(0xFF2E7D32) // Verde intenso
        efficiency >= 70 -> if (darkTheme) Color(0xFFFFF176) else Color(0xFFFBC02D) // Amarillo vibrante
        efficiency >= 55 -> if (darkTheme) Color(0xFFFFA726) else Color(0xFFEF6C00) // Naranja fuerte
        else -> if (darkTheme) Color(0xFFE57373) else Color(0xFFC62828) // Rojo visible
    }
}

@Composable
private fun getDynamicColor(lightColor: Color, darkColor: Color): Color {
    return if (isSystemInDarkTheme()) darkColor else lightColor
}