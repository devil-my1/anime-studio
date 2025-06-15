package com.sukuna.animestudio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C4DFF), // Deep Purple
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5E35B1),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF00BFA5), // Teal
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00897B),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFF4081), // Pink
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC2185B),
    onTertiaryContainer = Color.White,
    background = Color(0xFF121212), // Dark Background
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E), // Slightly lighter dark
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D2D), // Even lighter dark
    onSurfaceVariant = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color.White
)

private val LightColorScheme = darkColorScheme( // Using dark scheme for both to maintain consistency
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5E35B1),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF00BFA5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00897B),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFF4081),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC2185B),
    onTertiaryContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color.White
)

@Composable
fun AnimeStudioTheme(
    darkTheme: Boolean = true, // Always use dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}