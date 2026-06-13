package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CreatorOSDarkColorScheme = darkColorScheme(
    primary = MoneyGreen,
    onPrimary = DarkBackground,
    secondary = DarkTextSecondary,
    onSecondary = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextPrimary,
    error = WarningRed,
    onError = DarkTextPrimary,
    outline = DarkBorder
)

private val CreatorOSLightColorScheme = lightColorScheme(
    primary = MoneyGreen,
    onPrimary = LightBackground,
    secondary = LightTextSecondary,
    onSecondary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextPrimary,
    error = WarningRed,
    onError = LightTextPrimary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) CreatorOSDarkColorScheme else CreatorOSLightColorScheme,
        typography = Typography,
        content = content
    )
}
