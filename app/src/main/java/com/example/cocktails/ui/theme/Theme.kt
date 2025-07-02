package com.example.cocktails.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = OrangeSecondary,
    onSecondary = Color.Black,
    background = BlackBackground,
    onBackground = WhiteText,
    surface = BlackSurface,
    onSurface = WhiteText
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.Black,
    secondary = OrangeSecondary,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = DarkText,
    surface = Color(0xFFF5F5F5),
    onSurface = DarkText
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