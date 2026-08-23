package com.shahsurveyors.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricGold,
    secondary = TealAccent,
    tertiary = Navy,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    secondary = TealAccent,
    tertiary = ElectricGold,
    background = Color(0xFFF5F5F5),
    surface = Color.White
)

@Composable
fun ShahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
