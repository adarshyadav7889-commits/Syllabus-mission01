package com.adarsh.syllabusmission.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F5D8A),
    onPrimary = Color.White,
    background = Color(0xFFFBF8F1),
    onBackground = Color(0xFF1F2933),
    surface = Color(0xFFFBF8F1),
    onSurface = Color(0xFF1F2933),
    surfaceVariant = Color(0xFFE6E1D6),
    onSurfaceVariant = Color(0xFF4A5560),
    outline = Color(0xFF4A5560)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DB8E8),
    onPrimary = Color(0xFF0B2239),
    background = Color(0xFF14181F),
    onBackground = Color(0xFFE8E6E1),
    surface = Color(0xFF14181F),
    onSurface = Color(0xFFE8E6E1),
    surfaceVariant = Color(0xFF2A313B),
    onSurfaceVariant = Color(0xFFB4BBC4),
    outline = Color(0xFF8A93A0)
)

@Composable
fun SyllabusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
