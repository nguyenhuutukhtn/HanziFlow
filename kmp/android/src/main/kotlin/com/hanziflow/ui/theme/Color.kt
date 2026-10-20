package com.hanziflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Amber accent and the three text-role colours from docs/ui/tokens.md §5.1.
// Dynamic (wallpaper) colour is intentionally off: fixed contrast between Hanzi,
// pinyin and Vietnamese keeps the transcript legible on every device.

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF2B155),
    onPrimary = Color(0xFF2A1800),
    primaryContainer = Color(0xFF4A350F),
    onPrimaryContainer = Color(0xFFFFE2B0),
    secondary = Color(0xFF7FD6C4),
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF1E4A40),
    onSecondaryContainer = Color(0xFFA6F2DF),
    tertiary = Color(0xFF9FB4D6),
    onTertiary = Color(0xFF10304F),
    background = Color(0xFF15161A),
    onBackground = Color(0xFFECECEE),
    surface = Color(0xFF15161A),
    onSurface = Color(0xFFECECEE),
    surfaceVariant = Color(0xFF23252B),
    onSurfaceVariant = Color(0xFFA7A9B3),
    outline = Color(0xFF8A8C96),
    outlineVariant = Color(0xFF3F4149),
    error = Color(0xFFF2A6A6),
    onError = Color(0xFF3B0909),
    errorContainer = Color(0xFF4A2020),
    onErrorContainer = Color(0xFFFFDAD6),
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8A5A1E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFCE3B8),
    onPrimaryContainer = Color(0xFF2A1800),
    secondary = Color(0xFF1C6E5D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA7F2E1),
    onSecondaryContainer = Color(0xFF00201A),
    tertiary = Color(0xFF3E5A85),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBF6),
    onBackground = Color(0xFF1B1C1F),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF1B1C1F),
    surfaceVariant = Color(0xFFEDE6DA),
    onSurfaceVariant = Color(0xFF5C5E66),
    outline = Color(0xFF7B7D86),
    outlineVariant = Color(0xFFCAC4B8),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

/** Project-defined roles Material 3 has no slot for (tokens.md §5.1). */
@Immutable
data class AppColors(
    val successGreen: Color,
    val meaning: Color,
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(successGreen = Color(0xFF8FD17A), meaning = Color(0xFF9FB4D6))
}

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
