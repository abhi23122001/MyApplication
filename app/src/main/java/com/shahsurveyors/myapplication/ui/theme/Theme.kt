package com.shahsurveyors.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ShahGreen,
    onPrimary = ShahWhite,

    primaryContainer = ShahLightGreen,
    onPrimaryContainer = ShahDarkGreen,

    secondary = ShahDarkGreen,
    onSecondary = ShahWhite,

    tertiary = SuccessGreen,
    onTertiary = ShahWhite,

    background = ShahGrey,
    onBackground = ShahBlack,

    surface = ShahWhite,
    onSurface = ShahBlack,

    surfaceVariant = ShahLightGrey,
    onSurfaceVariant = ShahDarkGrey,

    error = ErrorRed,
    onError = ShahWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = ShahLightGreen,
    onPrimary = ShahDarkGreen,

    primaryContainer = ShahDarkGreen,
    onPrimaryContainer = ShahLightGreen,

    secondary = ShahGreen,
    onSecondary = ShahWhite,

    tertiary = SuccessGreen,
    onTertiary = ShahWhite,

    background = DeepMidnightSlate,
    onBackground = ShahWhite,

    surface = DeepMidnightSlate,
    onSurface = ShahWhite,

    surfaceVariant = ShahDarkGrey,
    onSurfaceVariant = ShahLightGrey,

    error = ErrorRed,
    onError = ShahWhite
)

@Composable
fun ShahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}