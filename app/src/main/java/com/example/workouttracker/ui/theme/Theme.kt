package com.example.workouttracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun buildM3ColorScheme(glassColors: GlassThemeColors): ColorScheme {
    return if (glassColors.isDark) {
        darkColorScheme(
            primary = glassColors.primary,
            onPrimary = Color.Black,
            primaryContainer = glassColors.primaryVariant,
            onPrimaryContainer = Color.White,
            secondary = glassColors.secondary,
            onSecondary = Color.Black,
            background = glassColors.backgroundStart,
            onBackground = glassColors.textPrimary,
            surface = glassColors.surfaceGlass,
            onSurface = glassColors.textPrimary,
            surfaceVariant = glassColors.surfaceGlassVariant,
            onSurfaceVariant = glassColors.textSecondary,
            outline = glassColors.borderGlass
        )
    } else {
        lightColorScheme(
            primary = glassColors.primary,
            onPrimary = Color.White,
            primaryContainer = glassColors.backgroundEnd,
            onPrimaryContainer = glassColors.primaryVariant,
            secondary = glassColors.secondary,
            onSecondary = Color.White,
            background = glassColors.backgroundStart,
            onBackground = glassColors.textPrimary,
            surface = glassColors.surfaceGlass,
            onSurface = glassColors.textPrimary,
            surfaceVariant = glassColors.surfaceGlassVariant,
            onSurfaceVariant = glassColors.textSecondary,
            outline = glassColors.borderGlass
        )
    }
}

@Composable
fun IronForgeTheme(
    themeMode: IronForgeThemeMode = IronForgeThemeMode.MIDNIGHT_OBSIDIAN,
    content: @Composable () -> Unit
) {
    val glassColors = getGlassColors(themeMode)
    val colorScheme = buildM3ColorScheme(glassColors)

    IronForgeGlassTheme(themeMode = themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
