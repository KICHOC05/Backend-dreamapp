package com.example.appmobile.presentation.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmobile.data.repository.HeartRateRepository
import com.example.appmobile.presentation.ui.components.BPMLineChart

@Composable
fun BPMLineScreen() {
    //historial de ritmo cardiaco
    val bpmHistory = HeartRateRepository.bpmHistory.collectAsState()

    //Historial del ritmo cardiaco
    Text(text = "Historial de BPM (últimos ${bpmHistory.value.size})")
    BPMLineChart(
        bpmValues = bpmHistory.value,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    //Limpia el historial
    Button(onClick = {
        HeartRateRepository.clearHistory()
    }) {
        Text(text = "Limpiar historial")
    }
}