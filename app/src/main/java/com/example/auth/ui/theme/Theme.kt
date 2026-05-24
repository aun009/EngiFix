package com.example.auth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EngifixColorScheme = lightColorScheme(
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

@Composable
fun AuthTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EngifixColorScheme,
        typography = Typography,
        content = content
    )
}
