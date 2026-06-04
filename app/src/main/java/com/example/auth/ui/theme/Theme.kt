package com.example.auth.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EngiFixLightColorScheme = lightColorScheme(
    primary          = Clay,
    onPrimary        = Color.White,
    primaryContainer = ClaySoft,
    onPrimaryContainer = Color(0xFF4B1D0D),

    secondary        = Sage,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFE3ECD9),
    onSecondaryContainer = Color(0xFF1D2A18),

    tertiary         = Tide,
    onTertiary       = Color.White,
    tertiaryContainer  = Color(0xFFD7EFEC),
    onTertiaryContainer = Color(0xFF092A28),

    error            = Color(0xFFB3261E),
    errorContainer   = Color(0xFFF9DEDC),
    onError          = Color.White,
    onErrorContainer = Color(0xFF410E0B),

    background       = Canvas,
    onBackground     = Ink,

    surface          = Paper,
    onSurface        = Ink,
    surfaceVariant   = Color(0xFFEEE8DC),
    onSurfaceVariant = SoftInk,

    outline          = Color(0xFF8B8376),
    outlineVariant   = Line,

    inverseSurface   = Ink,
    inverseOnSurface = Paper,
)

private val EngiFixDarkColorScheme = darkColorScheme(
    primary          = Color(0xFFFFB49B),
    onPrimary        = Color(0xFF5F1600),
    primaryContainer = Color(0xFF8C3418),
    onPrimaryContainer = Color(0xFFFFDBCF),

    secondary        = Color(0xFFBFCBAE),
    onSecondary      = Color(0xFF273420),
    secondaryContainer = Color(0xFF3D4B35),
    onSecondaryContainer = Color(0xFFDBE7CE),

    tertiary         = Color(0xFFA6D5D0),
    onTertiary       = Color(0xFF073735),
    tertiaryContainer  = Color(0xFF23504D),
    onTertiaryContainer = Color(0xFFC2F1EC),

    error            = Color(0xFFFFB4AB),
    errorContainer   = Color(0xFF93000A),
    onError          = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background       = Color(0xFF141311),
    onBackground     = Color(0xFFE9E1D9),

    surface          = Color(0xFF1D1B18),
    onSurface        = Color(0xFFE9E1D9),
    surfaceVariant   = Color(0xFF4E453F),
    onSurfaceVariant = Color(0xFFD3C3B9),

    outline          = Color(0xFF9C8E84),
    outlineVariant   = Color(0xFF4E453F),

    inverseSurface   = Color(0xFFE9E1D9),
    inverseOnSurface = Color(0xFF32302D),
)

@Composable
fun AuthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EngiFixDarkColorScheme else EngiFixLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
