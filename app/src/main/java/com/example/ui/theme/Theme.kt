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
    primary = XBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F3854),
    onPrimaryContainer = Color(0xFFBDE4FF),
    secondary = XAccentPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E2248),
    onSecondaryContainer = Color(0xFFE9D8FD),
    background = XDarkBg,
    onBackground = TextPrimaryDark,
    surface = XDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = XDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = XDarkBorder,
    outlineVariant = Color(0xFF21262D)
)

private val LightColorScheme = lightColorScheme(
    primary = XBlue,
    onPrimary = Color.White,
    primaryContainer = XBlueLight,
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = XAccentPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF581C87),
    background = XLightBg,
    onBackground = TextPrimaryLight,
    surface = XLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = XLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = XLightBorder,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
