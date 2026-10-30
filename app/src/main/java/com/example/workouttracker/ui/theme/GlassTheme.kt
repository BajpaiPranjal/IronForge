package com.example.workouttracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class IronForgeThemeMode(val displayName: String, val subtitle: String, val isDark: Boolean) {
    FROST_CYAN("Frost Cyan", "Light • Crisp Ice Gradient", false),
    WARM_ROSE("Warm Pastel Rose", "Light • Sunrise Peach Gradient", false),
    EMERALD_MINT("Emerald Mint", "Light • Soft Forest Gradient", false),
    MIDNIGHT_OBSIDIAN("Midnight Obsidian", "Dark • Deep Space Gradient", true),
    CYBER_TEAL("Deep Cyber Teal", "Dark • Dark Slate Gradient", true),
    SOLAR_EMBER("Solar Ember", "Dark • Carbon Obsidian Gradient", true)
}

@Immutable
data class GlassThemeColors(
    val mode: IronForgeThemeMode,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val surfaceGlass: Color,
    val surfaceGlassVariant: Color,
    val borderGlass: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentGlow: Color,
    val isDark: Boolean
) {
    val backgroundBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(backgroundStart, backgroundEnd)
        )
}

val FrostCyanColors = GlassThemeColors(
    mode = IronForgeThemeMode.FROST_CYAN,
    primary = Color(0xFF00BCD4),
    primaryVariant = Color(0xFF0097A7),
    secondary = Color(0xFF0288D1),
    backgroundStart = Color(0xFFE0F7FA),
    backgroundEnd = Color(0xFFE1F5FE),
    surfaceGlass = Color(0xFFFFFFFF).copy(alpha = 0.40f),
    surfaceGlassVariant = Color(0xFFFFFFFF).copy(alpha = 0.28f),
    borderGlass = Color(0xFF00BCD4).copy(alpha = 0.35f),
    textPrimary = Color(0xFF0A2B31),
    textSecondary = Color(0xFF37636B),
    accentGlow = Color(0xFF00E5FF).copy(alpha = 0.28f),
    isDark = false
)

val WarmRoseColors = GlassThemeColors(
    mode = IronForgeThemeMode.WARM_ROSE,
    primary = Color(0xFFF06292),
    primaryVariant = Color(0xFFD81B60),
    secondary = Color(0xFFFF7043),
    backgroundStart = Color(0xFFFFF3E0),
    backgroundEnd = Color(0xFFFCE4EC),
    surfaceGlass = Color(0xFFFFFFFF).copy(alpha = 0.45f),
    surfaceGlassVariant = Color(0xFFFFFFFF).copy(alpha = 0.32f),
    borderGlass = Color(0xFFF06292).copy(alpha = 0.35f),
    textPrimary = Color(0xFF3E1F27),
    textSecondary = Color(0xFF6E434D),
    accentGlow = Color(0xFFFF80AB).copy(alpha = 0.28f),
    isDark = false
)

val EmeraldMintColors = GlassThemeColors(
    mode = IronForgeThemeMode.EMERALD_MINT,
    primary = Color(0xFF00C853),
    primaryVariant = Color(0xFF009624),
    secondary = Color(0xFF00BFA5),
    backgroundStart = Color(0xFFE8F5E9),
    backgroundEnd = Color(0xFFF1F8E9),
    surfaceGlass = Color(0xFFFFFFFF).copy(alpha = 0.45f),
    surfaceGlassVariant = Color(0xFFFFFFFF).copy(alpha = 0.32f),
    borderGlass = Color(0xFF00C853).copy(alpha = 0.35f),
    textPrimary = Color(0xFF102E1A),
    textSecondary = Color(0xFF3B6647),
    accentGlow = Color(0xFF69F0AE).copy(alpha = 0.30f),
    isDark = false
)

val MidnightObsidianColors = GlassThemeColors(
    mode = IronForgeThemeMode.MIDNIGHT_OBSIDIAN,
    primary = Color(0xFF7C4DFF),
    primaryVariant = Color(0xFF651FFF),
    secondary = Color(0xFF00E5FF),
    backgroundStart = Color(0xFF0F0C20),
    backgroundEnd = Color(0xFF1A162B),
    surfaceGlass = Color(0xFF251F3D).copy(alpha = 0.50f),
    surfaceGlassVariant = Color(0xFF1B162E).copy(alpha = 0.38f),
    borderGlass = Color(0xFF7C4DFF).copy(alpha = 0.45f),
    textPrimary = Color(0xFFF5F3FC),
    textSecondary = Color(0xFFB3A8D4),
    accentGlow = Color(0xFF7C4DFF).copy(alpha = 0.35f),
    isDark = true
)

val CyberTealColors = GlassThemeColors(
    mode = IronForgeThemeMode.CYBER_TEAL,
    primary = Color(0xFF00E676),
    primaryVariant = Color(0xFF00B0FF),
    secondary = Color(0xFF1DE9B6),
    backgroundStart = Color(0xFF0D1B2A),
    backgroundEnd = Color(0xFF1B263B),
    surfaceGlass = Color(0xFF415A77).copy(alpha = 0.35f),
    surfaceGlassVariant = Color(0xFF1B263B).copy(alpha = 0.45f),
    borderGlass = Color(0xFF00E676).copy(alpha = 0.40f),
    textPrimary = Color(0xFFE2EAF2),
    textSecondary = Color(0xFF8FA8C2),
    accentGlow = Color(0xFF00E676).copy(alpha = 0.32f),
    isDark = true
)

val SolarEmberColors = GlassThemeColors(
    mode = IronForgeThemeMode.SOLAR_EMBER,
    primary = Color(0xFFFFAB00),
    primaryVariant = Color(0xFFFF6D00),
    secondary = Color(0xFFFFD740),
    backgroundStart = Color(0xFF121212),
    backgroundEnd = Color(0xFF1E1E1E),
    surfaceGlass = Color(0xFF2C2C2C).copy(alpha = 0.40f),
    surfaceGlassVariant = Color(0xFF242424).copy(alpha = 0.32f),
    borderGlass = Color(0xFFFFAB00).copy(alpha = 0.42f),
    textPrimary = Color(0xFFFBF7F0),
    textSecondary = Color(0xFFBDB3A3),
    accentGlow = Color(0xFFFFAB00).copy(alpha = 0.35f),
    isDark = true
)

fun getGlassColors(mode: IronForgeThemeMode): GlassThemeColors {
    return when (mode) {
        IronForgeThemeMode.FROST_CYAN -> FrostCyanColors
        IronForgeThemeMode.WARM_ROSE -> WarmRoseColors
        IronForgeThemeMode.EMERALD_MINT -> EmeraldMintColors
        IronForgeThemeMode.MIDNIGHT_OBSIDIAN -> MidnightObsidianColors
        IronForgeThemeMode.CYBER_TEAL -> CyberTealColors
        IronForgeThemeMode.SOLAR_EMBER -> SolarEmberColors
    }
}

val LocalGlassColors = staticCompositionLocalOf { MidnightObsidianColors }

@Composable
fun IronForgeGlassTheme(
    themeMode: IronForgeThemeMode = IronForgeThemeMode.MIDNIGHT_OBSIDIAN,
    content: @Composable () -> Unit
) {
    val colors = getGlassColors(themeMode)
    CompositionLocalProvider(LocalGlassColors provides colors) {
        content()
    }
}
