package com.vibe.social.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VibeAccentBlue,
    onPrimary = VibeBlack,
    primaryContainer = VibeDarkCard,
    onPrimaryContainer = VibeTextPrimary,
    secondary = VibeTextSecondary,
    onSecondary = VibeBlack,
    tertiary = VibeLikeRed,
    background = VibeBlack,
    onBackground = VibeTextPrimary,
    surface = VibeBlack,
    onSurface = VibeTextPrimary,
    surfaceVariant = VibeDarkSurface,
    onSurfaceVariant = VibeTextSecondary,
    outline = VibeBorder,
    outlineVariant = VibeLightBorder
)

@Composable
fun VibeTheme(
    darkTheme: Boolean = true, // Always AMOLED dark aesthetic for Instagram / Vibe
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VibeTypography,
        content = content
    )
}
