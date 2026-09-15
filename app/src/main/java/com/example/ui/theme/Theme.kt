package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color(0xFF003822),
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = MintLight,
    secondary = MintLight,
    onSecondary = Color(0xFF003822),
    tertiary = RiskWatch,
    background = DarkCanvas,
    onBackground = TextWhitePrimary,
    surface = DarkSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextWhiteSecondary,
    error = RiskCritical,
    outline = DarkGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EEDF),
    onPrimaryContainer = ForestGreenDark,
    secondary = ForestGreenDark,
    onSecondary = Color.White,
    tertiary = RiskWatch,
    background = LightCanvas,
    onBackground = TextDarkPrimary,
    surface = LightSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextDarkSecondary,
    error = RiskCritical,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to rich dark tactical theme as shown in Figma screenshots
    dynamicColor: Boolean = false, // Keep branded emerald aesthetic consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
