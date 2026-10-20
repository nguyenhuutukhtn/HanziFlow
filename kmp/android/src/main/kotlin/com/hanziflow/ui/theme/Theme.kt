package com.hanziflow.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** 4 / 8 / 12 / 16 / 24 / 32dp — one scale for the whole app (tokens.md §5.3). */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}

/**
 * Amber Material 3 theme. Dark is the app default regardless of the system
 * setting (tokens.md Principle 5); the preference is stored by SettingsRepository
 * and will get a Settings toggle in G2b.
 */
@Composable
fun HanziFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) {
            AppColors(successGreen = Color(0xFF8FD17A), meaning = Color(0xFF9FB4D6))
        } else {
            AppColors(successGreen = Color(0xFF2E7D32), meaning = Color(0xFF3E5A85))
        },
        LocalAppTextStyles provides AppTextStyles(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HanziFlowTypography,
            shapes = Shapes(),
            content = content,
        )
    }
}
