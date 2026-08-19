package com.example.appmobile.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//Graficas elaboradas con Canvas  del ritmo cardiaco
@Composable
fun BPMLineChart(bpmValues: List<Float>, modifier: Modifier = Modifier) {
    val maxBpm = (bpmValues.maxOrNull() ?: 100f).coerceAtLeast(100f)
    val minBpm = (bpmValues.minOrNull() ?: 60f).coerceAtMost(60f)

    Canvas(modifier = modifier) {
        val spacing = size.width / (bpmValues.size.coerceAtLeast(1))
        val height = size.height

        for (i in 1 until bpmValues.size) {
            val x1 = spacing * (i - 1)
            val y1 = height - ((bpmValues[i - 1] - minBpm) / (maxBpm - minBpm)) * height

            val x2 = spacing * i
            val y2 = height - ((bpmValues[i] - minBpm) / (maxBpm - minBpm)) * height

            drawLine(
                color = androidx.compose.ui.graphics.Color.Red,
                start = androidx.compose.ui.geometry.Offset(x1, y1),
                end = androidx.compose.ui.geometry.Offset(x2, y2),
                strokeWidth = 4f
            )
        }
    }
}

