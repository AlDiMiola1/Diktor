package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioAmber,
    onPrimary = Color(0xFF0B0F19),
    primaryContainer = StudioAmberDark,
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = StudioCyan,
    onSecondary = Color(0xFF0B0F19),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = StudioEmerald,
    onTertiary = Color(0xFF0B0F19),
    error = StudioRose,
    onError = Color.White,
    background = StudioBg,
    onBackground = StudioTextPrimary,
    surface = StudioCard,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurface,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder,
    outlineVariant = StudioBorder.copy(alpha = 0.6f)
)

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun DiktorProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = AppTypography,
        shapes = StudioShapes,
        content = content
    )
}
