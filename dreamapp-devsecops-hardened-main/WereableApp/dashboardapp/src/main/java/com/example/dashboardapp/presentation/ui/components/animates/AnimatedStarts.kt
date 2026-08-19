package com.example.dashboardapp.presentation.ui.components.animates

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AnimatedBackground(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(Color(0xFF6E528A), Color.Black)
    } else {
        listOf(Color(0xFF6E528A), Color.White)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        FallingStars(isDarkTheme = isDark)
        content()
    }
}

@Composable
fun FallingStars(isDarkTheme: Boolean) {
    val starColor = if (isDarkTheme) Color.White else Color.Black
    val stars = remember { List(25) { Star(sizeRange = 5f..8f) } }
    val infiniteTransition = rememberInfiniteTransition()

    stars.forEach { star ->
        val delay = (0..5000).random()

        val yOffset by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 1100f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = star.speed,
                    delayMillis = delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 10000,
                    delayMillis = delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        // Alpha para hacer fade-in/fade-out basado en la posición Y (más suave)
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = star.speed / 4,
                    delayMillis = delay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            with(drawContext.canvas) {
                save()
                translate(star.x, yOffset + star.startY)
                rotate(rotation)
                drawPath(
                    path = createStarPath(
                        centerX = 0f,
                        centerY = 0f,
                        radius = star.size
                    ),
                    color = starColor.copy(alpha = 0.3f + 0.7f * alpha) // base 0.3 + fade
                )
                restore()
            }
        }
    }
}


fun createStarPath(centerX: Float, centerY: Float, radius: Float): androidx.compose.ui.graphics.Path {
    val path = androidx.compose.ui.graphics.Path()
    val innerRadius = radius / 2.5f
    val angle = Math.PI / 5 // 36 grados

    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = (centerX + r * Math.cos(i * angle - Math.PI / 2)).toFloat()
        val y = (centerY + r * Math.sin(i * angle - Math.PI / 2)).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

data class Star(
    val x: Float = (0..1080).random().toFloat(),
    val startY: Float = (0..1920).random().toFloat(),
    val size: Float,
    val speed: Int = listOf(4000, 5000, 6000).random()
) {
    constructor(sizeRange: ClosedFloatingPointRange<Float>) : this(
        x = (0..1080).random().toFloat(),
        startY = (0..1920).random().toFloat(),
        size = (sizeRange.start..sizeRange.endInclusive).randomFloat(),
        speed = listOf(4000, 5000, 6000).random()
    )
}

fun ClosedFloatingPointRange<Float>.randomFloat(): Float {
    return (start + Math.random() * (endInclusive - start)).toFloat()
}