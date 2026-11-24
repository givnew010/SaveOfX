package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val XBlue = Color(0xFF1D9BF0)
val XBlueDark = Color(0xFF1A8CD8)
val XBlack = Color(0xFF0F1419)
val XDarkSurface = Color(0xFF161B22)
val XDarkBackground = Color(0xFF0D1117)
val XDarkBorder = Color(0xFF30363D)
val XDarkElevated = Color(0xFF21262D)
val XDarkSecondaryText = Color(0xFF8B949E)

val XLightBackground = Color(0xFFF7F9F9)
val XLightSurface = Color(0xFFFFFFFF)
val XLightBorder = Color(0xFFE2E8F0)
val XLightSurfaceVariant = Color(0xFFEFF3F4)
val XLightSecondaryText = Color(0xFF536471)

val FavoriteRed = Color(0xFFEF4444)
val NoteAmber = Color(0xFFF59E0B)
val NoteAmberDarkText = Color(0xFFD97706)
val SuccessGreen = Color(0xFF10B981)
val PurpleAccent = Color(0xFF8B5CF6)

fun parseHexColor(hex: String, fallback: Color = XBlue): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
