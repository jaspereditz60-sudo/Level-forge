package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForgeDarkPrimary,
    onPrimary = Color.Black,
    secondary = ForgeDarkSecondary,
    onSecondary = Color.Black,
    background = ForgeDarkBg,
    onBackground = Color.White,
    surface = ForgeDarkSurface,
    onSurface = Color.White,
    error = ForgeDarkError
)

private val LightColorScheme = lightColorScheme(
    primary = ForgeLightPrimary,
    onPrimary = Color.White,
    secondary = ForgeLightSecondary,
    onSecondary = Color.Black,
    background = ForgeLightBg,
    onBackground = Color(0xFF1E2028),
    surface = ForgeLightSurface,
    onSurface = Color(0xFF1E2028),
    error = ForgeLightError
)

@Composable
fun LevelForgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
