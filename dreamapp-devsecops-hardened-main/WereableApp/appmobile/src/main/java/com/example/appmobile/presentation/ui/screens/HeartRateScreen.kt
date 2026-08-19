package com.example.appmobile.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmobile.data.repository.HeartRateRepository

import androidx.compose.runtime.collectAsState

@Composable
fun HeartRateScreen(modifier: Modifier = Modifier) {
    //cambio el collectAsState
    val bpm by HeartRateRepository.bpm.collectAsState()

    Log.d("HeartRateScreen", "Renderizando con BPM: $bpm")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Frecuencia Cardíaca", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (bpm > 0) "${bpm.toInt()} BPM" else "Esperando datos...",
            fontSize = 36.sp
        )
    }
}