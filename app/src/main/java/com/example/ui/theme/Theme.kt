package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = XBlue,
    onPrimary = Color.White,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = XDarkBackground,
    onBackground = Color(0xFFF0F6FC),
    surface = XDarkSurface,
    onSurface = Color(0xFFF0F6FC),
    surfaceVariant = XDarkElevated,
    onSurfaceVariant = XDarkSecondaryText,
    outline = XDarkBorder,
    error = FavoriteRed
)

private val LightColorScheme = lightColorScheme(
    primary = XBlue,
    onPrimary = Color.White,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = XLightBackground,
    onBackground = XBlack,
    surface = XLightSurface,
    onSurface = XBlack,
    surfaceVariant = XLightSurfaceVariant,
    onSurfaceVariant = XLightSecondaryText,
    outline = XLightBorder,
    error = FavoriteRed
)

@Composable
fun XFoldersTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = XFoldersTypography,
        content = content
    )
}
