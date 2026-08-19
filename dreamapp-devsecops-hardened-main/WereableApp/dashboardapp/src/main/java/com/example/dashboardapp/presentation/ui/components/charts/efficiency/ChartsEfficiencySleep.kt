package com.example.dashboardapp.presentation.ui.components.charts.efficiency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dashboardapp.presentation.viewmodel.sleep.SleepStatsUiState

@Composable
fun ChartsEfficiencySleep(uiState: SleepStatsUiState) {

    var colort = MaterialTheme.colorScheme.primary;

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            var chartMode by remember { mutableStateOf("last7Days") } // Start filter
            when (uiState) {
                is SleepStatsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SleepStatsUiState.Success -> {
                    val stats = (uiState as SleepStatsUiState.Success).data

                    val buttonItems = listOf(
                        "last7Days" to "7D",
                        "lastMonth" to "1M",
                        "last6Months" to "6M",
                        "lastYaer" to "1A"
                    )

                    // Show the chart for 7D y 1M, 6M and 1Y
                    when (chartMode) {
                        "last7Days" -> {
                            val points = stats.efficiency.last7Days
                            val xVals = points.map { it.date }
                            val yVals = points.map { it.efficiency.toFloat() }
                            val title = "Eficiencia de sueño en los últimos 7 días"
                            val desc = "Porcentaje de eficiencia diaria durante la última semana."
                            val formatter: (List<String>, Float) -> String = { xList, value ->
                                xList.getOrNull(value.toInt())?.let { dateStr ->
                                    try {
                                        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val date = java.time.LocalDate.parse(dateStr, formatter)
                                        date.dayOfWeek.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercase() }
                                    } catch (e: Exception) {
                                        "-"
                                    }
                                } ?: "-"
                            }
                            if (yVals.isNotEmpty()) {
                                LineChart(
                                    x = xVals,
                                    y = yVals,
                                    title = title,
                                    description = desc,
                                    xValueFormatter = formatter
                                )
                            } else {
                                Text(
                                    "No hay datos de eficiencia para el periodo seleccionado.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        "lastMonth" -> {
                            val points = stats.efficiency.lastMonth
                            val xVals = (1..points.size).map { it.toString() }
                            val yVals = points.map { it.efficiency.toFloat() }
                            val title = "Eficiencia de sueño en el último mes"
                            val desc = "Porcentaje de eficiencia diaria durante los últimos 30 días."
                            val formatter: (List<String>, Float) -> String = { xList, value ->
                                xList.getOrNull(value.toInt()) ?: ""
                            }
                            if (yVals.isNotEmpty()) {
                                LineChart(
                                    x = xVals,
                                    y = yVals,
                                    title = title,
                                    description = desc,
                                    xValueFormatter = formatter
                                )
                            } else {
                                Text(
                                    "No hay datos de eficiencia para el periodo seleccionado.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        "last6Months" -> {
                            val points = stats.efficiency.last6Months
                            val xVals = points.map { it.date }
                            val yVals = points.map { it.efficiency.toFloat() }
                            val title = "Eficiencia de sueño en los últimos 6 meses"
                            val desc = "Promedio de eficiencia mensual durante los últimos 6 meses."
                            val formatter: (List<String>, Float) -> String = { xList, value ->
                                xList.getOrNull(value.toInt())?.let { monthStr ->
                                    try {
                                        val parts = monthStr.split("-")
                                        if (parts.size == 2) {
                                            val monthNum = parts[1].toIntOrNull()
                                            val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                                            monthNames.getOrNull((monthNum ?: 1) - 1) ?: monthStr
                                        } else monthStr
                                    } catch (e: Exception) {
                                        monthStr
                                    }
                                } ?: "-"
                            }
                            if (yVals.isNotEmpty()) {
                                LineChart(
                                    x = xVals,
                                    y = yVals,
                                    title = title,
                                    description = desc,
                                    xValueFormatter = formatter
                                )
                            } else {
                                Text(
                                    "No hay datos de eficiencia para el periodo seleccionado.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        "lastYaer" -> {
                            val points = stats.efficiency.lastYear
                            val xVals = points.map { it.date }
                            val yVals = points.map { it.efficiency.toFloat() }
                            val title = "Eficiencia de sueño en el ultimo año"
                            val desc = "Promedio de eficiencia mensual durante los último año."
                            val formatter: (List<String>, Float) -> String = { xList, value ->
                                xList.getOrNull(value.toInt())?.let { monthStr ->
                                    try {
                                        val parts = monthStr.split("-")
                                        if (parts.size == 2) {
                                            val monthNum = parts[1].toIntOrNull()
                                            val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                                            monthNames.getOrNull((monthNum ?: 1) - 1) ?: monthStr
                                        } else monthStr
                                    } catch (e: Exception) {
                                        monthStr
                                    }
                                } ?: "-"
                            }
                            if (yVals.isNotEmpty()) {
                                LineChart(
                                    x = xVals,
                                    y = yVals,
                                    title = title,
                                    description = desc,
                                    xValueFormatter = formatter
                                )
                            } else {
                                Text(
                                    "No hay datos de eficiencia para el periodo seleccionado.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        buttonItems.forEachIndexed { idx, (mode, label) ->
                            val isSelected = chartMode == mode
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clickable { chartMode = mode }
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
                is SleepStatsUiState.Error -> {
                    val errorMsg = (uiState as SleepStatsUiState.Error).message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Error: $errorMsg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}