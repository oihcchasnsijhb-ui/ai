package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    DARK("Obsidian HUD"),
    LIGHT("Daylight Lab")
}

private val JarvisDarkColorScheme = darkColorScheme(
    primary = CyanCore,
    onPrimary = CyanOnCore,
    primaryContainer = CyanContainer,
    onPrimaryContainer = CyanOnContainer,
    secondary = AmberArc,
    onSecondary = AmberOnArc,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    tertiary = EmeraldStatus,
    onTertiary = ObsidianBg,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldStatus,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianOutline,
    error = ErrorDark,
    onError = ObsidianBg,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorDark
)

private val JarvisLightColorScheme = lightColorScheme(
    primary = DaylightCyan,
    onPrimary = DaylightOnCyan,
    primaryContainer = DaylightCyanContainer,
    onPrimaryContainer = DaylightOnCyanContainer,
    secondary = DaylightAmber,
    onSecondary = DaylightOnAmber,
    secondaryContainer = DaylightAmberContainer,
    onSecondaryContainer = DaylightOnAmberContainer,
    tertiary = DaylightEmerald,
    onTertiary = DaylightSurface,
    background = DaylightBg,
    onBackground = TextPrimaryLight,
    surface = DaylightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DaylightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = DaylightOutline,
    error = ErrorLight,
    onError = DaylightSurface,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorLight
)

val JarvisShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun JarvisTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    MaterialTheme(
        colorScheme = if (useDark) JarvisDarkColorScheme else JarvisLightColorScheme,
        typography = JarvisTypography,
        shapes = JarvisShapes,
        content = content
    )
}
