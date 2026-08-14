package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = DarkSurfaceHighlight,
    onPrimaryContainer = AmberSecondary,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    tertiary = AmberSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceHighlight,
    onPrimaryContainer = AmberPrimary,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    tertiary = AmberSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek media dark mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

