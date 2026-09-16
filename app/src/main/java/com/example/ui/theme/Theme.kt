package com.example.ui.theme
 
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
 
@Immutable
data class AppColors(
    val isDark: Boolean,
    val canvas: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val card: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentLight: Color,
    val accentContainer: Color,
    val onAccent: Color,
    val bottomNavBg: Color,
    val topBarBg: Color,
    val divider: Color,
    val glassCard: Color,
    val glassBorder: Color,
    val inputBg: Color
)

val DarkAppColors = AppColors(
    isDark = true,
    canvas = DarkCanvas,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    card = Color(0xFF10211A),
    cardBorder = DarkGlassBorder,
    textPrimary = TextWhitePrimary,
    textSecondary = TextWhiteSecondary,
    textTertiary = Color.White.copy(alpha = 0.5f),
    accent = EmeraldAccent,
    accentLight = MintLight,
    accentContainer = ForestGreenDark,
    onAccent = Color(0xFF003822),
    bottomNavBg = Color(0xF209140F),
    topBarBg = Color(0xF20A1911),
    divider = Color.White.copy(alpha = 0.08f),
    glassCard = DarkGlassCard,
    glassBorder = DarkGlassBorder,
    inputBg = Color(0xFF14241E)
)

val LightAppColors = AppColors(
    isDark = false,
    canvas = LightCanvas,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    card = Color.White,
    cardBorder = Color(0xFFD3DFD8),
    textPrimary = TextDarkPrimary,
    textSecondary = TextDarkSecondary,
    textTertiary = Color(0xFF64748B),
    accent = ForestGreenPrimary,
    accentLight = EmeraldAccent,
    accentContainer = Color(0xFFD4EEDF),
    onAccent = Color.White,
    bottomNavBg = Color(0xFAFFFFFF),
    topBarBg = Color(0xF8FFFFFF),
    divider = Color(0xFFE2ECE6),
    glassCard = LightGlassCard,
    glassBorder = LightGlassBorder,
    inputBg = LightInputBg
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

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
    darkTheme: Boolean = true, // Default to rich dark tactical theme or user preference
    dynamicColor: Boolean = false, // Keep branded emerald aesthetic consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
