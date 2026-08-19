package com.example.dashboardapp.presentation.ui.components.user

import com.example.dashboardapp.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dashboardapp.domain.model.user.Sex

@Composable
fun ImcIndicator(weight: Int, height: Int, sex: Sex) {
    val weightKg = weight.toDouble()
    val heightCm = height.toDouble()
    val heightM = heightCm / 100
    val imc = weightKg / (heightM * heightM)

    val ranges = listOf(
        10.0 to 18.5,  // Bajo peso
        18.5 to 25.0,  // Normal
        25.0 to 30.0,  // Sobrepeso
        30.0 to 40.0   // Obesidad
    )

    val colors = listOf(
        MaterialTheme.colorScheme.primary,          // Color principal (azul o lo que definas)
        MaterialTheme.colorScheme.secondary,        // Color secundario
        MaterialTheme.colorScheme.tertiary,         // Color terciario
        MaterialTheme.colorScheme.error
    )

    val selectedIndex = ranges.indexOfFirst { imc >= it.first && imc < it.second }.coerceAtLeast(0)

    val imageRes = when (sex) {
        Sex.MEN -> R.drawable.marte
        Sex.WOMAN -> R.drawable.venus
        else -> R.drawable.marte
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = imageRes,
            contentDescription = "Icono de género",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            ranges.forEachIndexed { index, (start, end) ->
                val color = colors[index]
                val weight = ((end - start) / 30.0).toFloat()

                val isSelected = index == selectedIndex
                val scale = if (isSelected) 1.2f else 1f
                val alpha = if (isSelected) 1f else 0.4f

                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleY = scale
                        }
                        .alpha(alpha)
                        .background(color)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(10.0, 18.5, 25.0, 30.0, 40.0).forEach { value ->
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val imcCategory = when (selectedIndex) {
            0 -> "Bajo peso"
            1 -> "Saludable"
            2 -> "Sobrepeso"
            else -> "Obesidad"
        }

        Text(
            text = "IMC: ${String.format("%.1f", imc)} – $imcCategory",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}