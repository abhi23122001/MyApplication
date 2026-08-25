package com.shahsurveyors.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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
    outline = ShahMediumGrey,
    outlineVariant = ShahLightGrey,
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
    outline = ShahMediumGrey,
    outlineVariant = ShahDarkGrey,
    error = ErrorRed,
    onError = ShahWhite
)

private val ShahShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun ShahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = ShahShapes,
        content = content
    )
}
