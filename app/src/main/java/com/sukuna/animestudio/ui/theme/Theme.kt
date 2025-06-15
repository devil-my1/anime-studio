package com.sukuna.animestudio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = deepPurple700,
    onPrimary = white,
    primaryContainer = deepPurple800,
    onPrimaryContainer = white,
    secondary = teal400,
    onSecondary = white,
    secondaryContainer = teal600,
    onSecondaryContainer = white,
    tertiary = pink500,
    onTertiary = white,
    tertiaryContainer = pink800,
    onTertiaryContainer = white,
    background = grey900,
    onBackground = white,
    surface = grey850,
    onSurface = white,
    surfaceVariant = grey800,
    onSurfaceVariant = white,
    error = redPink200,
    onError = white,
    errorContainer = red900,
    onErrorContainer = white
)

val LightColorScheme = lightColorScheme( // Using dark scheme for both to maintain consistency
    primary = deepPurple700,
    onPrimary = white,
    primaryContainer = deepPurple800,
    onPrimaryContainer = white,
    secondary = teal400,
    onSecondary = white,
    secondaryContainer = teal600,
    onSecondaryContainer = white,
    tertiary = pink500,
    onTertiary = white,
    tertiaryContainer = pink800,
    onTertiaryContainer = white,
    background = grey900,
    onBackground = white,
    surface = grey850,
    onSurface = white,
    surfaceVariant = grey800,
    onSurfaceVariant = white,
    error = redPink200,
    onError = white,
    errorContainer = red900,
    onErrorContainer = white
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