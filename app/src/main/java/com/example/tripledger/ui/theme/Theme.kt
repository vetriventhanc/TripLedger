package com.example.tripledger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFFFF8F00),

    background = Color.White,
    surface = Color.White,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,

    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFCC80),

    background = Color(0xFF121212),
    surface = Color(0xFF121212),

    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,

    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun TripLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors =
        if (darkTheme) {
            DarkColors
        } else {
            LightColors
        }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}