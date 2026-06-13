package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CreatorOSDarkColorScheme = darkColorScheme(
    primary = MoneyGreen,
    onPrimary = AppBackground,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextPrimary,
    error = WarningRed,
    onError = TextPrimary,
    outline = DarkBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CreatorOSDarkColorScheme,
        typography = Typography,
        content = content
    )
}
