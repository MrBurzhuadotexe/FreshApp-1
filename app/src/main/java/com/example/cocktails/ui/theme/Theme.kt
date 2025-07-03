package com.example.cocktails.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = primaryColor,
    onPrimary = Color.White,
    secondary = secondPrimary,
    onSecondary = Color.Black,
    background = whiteBackground,
    onBackground = blackText,
    surface = blackSurface,
    onSurface = blackText
)

private val LightColorScheme = lightColorScheme(
    primary = primaryColor,
    onPrimary = Color.Black,
    secondary = secondPrimary,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = whiteText,
    surface = Color(0xFFF5F5F5),
    onSurface = whiteText
)

@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}