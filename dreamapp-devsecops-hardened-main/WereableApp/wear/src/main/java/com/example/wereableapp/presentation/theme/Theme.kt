package com.example.wereableapp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Typography

// 1️⃣ Aquí se importa la paleta definida en Color.kt
import com.example.wereableapp.presentation.theme.md_theme_light_primary
import com.example.wereableapp.presentation.theme.md_theme_light_onPrimary
import com.example.wereableapp.presentation.theme.md_theme_light_background
import com.example.wereableapp.presentation.theme.md_theme_light_onBackground

// 2️⃣ Se define el esquema de colores para Wear Compose
private val WearColorPalette = Colors(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground
)

// 3️⃣ Tipografía base (opcionalmente puede crear Typography.kt para personalizar)
private val WearTypography = Typography()

// 4️⃣ Theme adaptado con paleta Material 3 Expressive
@Composable
fun WereableAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorPalette,
        typography = WearTypography,
        content = content
    )
}