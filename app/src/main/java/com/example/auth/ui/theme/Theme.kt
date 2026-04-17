package com.example.auth.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * EngiFix brand dark palette — all MaterialTheme.colorScheme references resolve to these.
 * This ensures zero inconsistency from device-dynamic colors.
 */
private val EngifixDarkColorScheme = darkColorScheme(
    primary          = Color(0xFF9D8FFF), // Brand purple (lighter for dark bg contrast)
    onPrimary        = Color(0xFF1C1C1E),
    primaryContainer = Color(0xFF3D3470), // Muted purple container
    onPrimaryContainer = Color(0xFFD4CEFF),

    secondary        = Color(0xFF8ECFF1), // Soft blue accent
    onSecondary      = Color(0xFF1C1C1E),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFCCE8FF),

    tertiary         = Color(0xFF81C784), // Soft green for success states
    onTertiary       = Color(0xFF1C1C1E),
    tertiaryContainer  = Color(0xFF1B3A1E),
    onTertiaryContainer = Color(0xFFC8E6C9),

    error            = Color(0xFFFF6B6B),
    errorContainer   = Color(0xFF3D1515),
    onError          = Color(0xFF1C1C1E),
    onErrorContainer = Color(0xFFFFCDD2),

    background       = Color(0xFF1C1C1E), // App-wide background
    onBackground     = Color(0xFFF5F5F5),

    surface          = Color(0xFF242426), // Card surfaces
    onSurface        = Color(0xFFF5F5F5),
    surfaceVariant   = Color(0xFF2C2C2E), // Slightly lighter surface
    onSurfaceVariant = Color(0xFFAAAAAA), // Muted text

    outline          = Color(0xFF3A3A3C), // Borders and dividers
    outlineVariant   = Color(0xFF2C2C2E),

    inverseSurface   = Color(0xFFF5F5F5),
    inverseOnSurface = Color(0xFF1C1C1E),
)

@Composable
fun AuthTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EngifixDarkColorScheme,
        typography = Typography,
        content = content
    )
}