package com.example.dashboardapp.presentation.ui.components.charts.efficiency

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dashboardapp.presentation.viewmodel.sleep.SleepPredictionEfficiencyNextMonthUiState

@Composable
fun ChartEfficiencySleepPredictionNextMonth(uiStatePrediction: SleepPredictionEfficiencyNextMonthUiState) {
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
        when (uiStatePrediction) {
            is SleepPredictionEfficiencyNextMonthUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SleepPredictionEfficiencyNextMonthUiState.Success -> {
                val prediction = uiStatePrediction.data
                val points = prediction.efficiencyPredictions
                val xVals = (1..points.size).map { it.toString() }
                val yVals = points.map { it.efficiency.toFloat() }
                val title = "Predicción de eficiencia de sueño para los próximos 30 días"
                val desc = "Porcentaje de eficiencia diaria estimada para los siguientes 30 días."
                val formatter: (List<String>, Float) -> String = { xList, value ->
                    xList.getOrNull(value.toInt()) ?: ""
                }
                if (yVals.isNotEmpty()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        LineChart(
                            x = xVals,
                            y = yVals,
                            title = title,
                            description = desc,
                            xValueFormatter = formatter
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            "No hay datos de predicción de eficiencia para los próximos 30 días.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            is SleepPredictionEfficiencyNextMonthUiState.Error -> {
                val errorMsg = uiStatePrediction.message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        "Error: $errorMsg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}