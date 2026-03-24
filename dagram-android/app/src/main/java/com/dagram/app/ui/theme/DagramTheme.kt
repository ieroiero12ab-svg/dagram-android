package com.dagram.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object DagramColors {
    val Background = Color(0xFF0F1923)
    val Surface = Color(0xFF162635)
    val Primary = Color(0xFF2196F3)
    val OnPrimary = Color.White
    val Secondary = Color(0xFF90CAF9)
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D1B2A),
    background = Color(0xFF0F1923),
    onBackground = Color.White,
    surface = Color(0xFF162635),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E2D3D),
    error = Color(0xFFEF5350),
    onError = Color.White
)

@Composable
fun DagramTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
