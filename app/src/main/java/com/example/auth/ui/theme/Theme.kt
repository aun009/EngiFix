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
    primary          = Color(0xFFEBA086),
    onPrimary        = Color(0xFF4B1D0D),
    primaryContainer = Color(0xFF6E3321),
    onPrimaryContainer = Color(0xFFF5D5C8),

    secondary        = Color(0xFFB5C4A6),
    onSecondary      = Color(0xFF22311F),
    secondaryContainer = Color(0xFF354333),
    onSecondaryContainer = Color(0xFFD7E4CB),

    tertiary         = Color(0xFF91CFC8),
    onTertiary       = Color(0xFF0A3432),
    tertiaryContainer  = Color(0xFF244D4A),
    onTertiaryContainer = Color(0xFFC9EAE6),

    error            = Color(0xFFFFB4AB),
    errorContainer   = Color(0xFF93000A),
    onError          = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background       = Color(0xFF181715),
    onBackground     = Color(0xFFE2D8CF),

    surface          = Color(0xFF211F1C),
    onSurface        = Color(0xFFE2D8CF),
    surfaceVariant   = Color(0xFF332F2A),
    onSurfaceVariant = Color(0xFFC9BBB0),

    outline          = Color(0xFF9B8D82),
    outlineVariant   = Color(0xFF4A433D),

    inverseSurface   = Color(0xFFE2D8CF),
    inverseOnSurface = Color(0xFF302D29),
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
