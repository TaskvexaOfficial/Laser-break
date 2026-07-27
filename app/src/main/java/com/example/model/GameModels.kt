package com.example.model

import androidx.compose.ui.graphics.Color

enum class StructureType {
    SEGMENTED_CIRCLE,
    SQUARE_LAYERS
}

data class Segment(
    val id: Int,
    val layerIndex: Int,
    val startAngle: Float,
    val sweepAngle: Float,
    val isDangerous: Boolean,
    var isDestroyed: Boolean = false,
    var health: Float = 100f // Laser depletes this
)

data class Layer(
    val index: Int,
    val radius: Float,
    val thickness: Float,
    val segments: List<Segment>,
    var currentRotation: Float,
    val rotationSpeed: Float,
    val isClockwise: Boolean
)

data class Structure(
    val type: StructureType,
    val layers: List<Layer>,
    val colorTheme: ColorTheme
)

data class ColorTheme(
    val safeColorPrimary: Color,
    val safeColorSecondary: Color,
    val safeColorGlow: Color,
    val dangerousColor: Color
)

val ThemeBlue = ColorTheme(
    safeColorPrimary = Color(0xFF00BFFF),
    safeColorSecondary = Color(0xFF1E90FF),
    safeColorGlow = Color(0xFF87CEFA),
    dangerousColor = Color(0xFF8B0000)
)

val ThemeCyan = ColorTheme(
    safeColorPrimary = Color(0xFF00FFFF),
    safeColorSecondary = Color(0xFF00CED1),
    safeColorGlow = Color(0xFFE0FFFF),
    dangerousColor = Color(0xFF8B0000)
)

val ThemePurple = ColorTheme(
    safeColorPrimary = Color(0xFF9370DB),
    safeColorSecondary = Color(0xFF8A2BE2),
    safeColorGlow = Color(0xFFD8BFD8),
    dangerousColor = Color(0xFF8B0000)
)

val ThemePink = ColorTheme(
    safeColorPrimary = Color(0xFFFF69B4),
    safeColorSecondary = Color(0xFFFF1493),
    safeColorGlow = Color(0xFFFFB6C1),
    dangerousColor = Color(0xFF8B0000)
)

val AllThemes = listOf(ThemeBlue, ThemeCyan, ThemePurple, ThemePink)

enum class GameStatus {
    IDLE,
    PLAYING,
    PAUSED,
    WON,
    LOST
}

data class GameState(
    val status: GameStatus = GameStatus.IDLE,
    val structure: Structure? = null,
    val isFiring: Boolean = false,
    val laserYPos: Float = 0f,
    val laserTipX: Float = 0f,
    val laserTipY: Float = 0f,
    val shakeOffsetX: Float = 0f,
    val shakeOffsetY: Float = 0f,
    val particles: List<Particle> = emptyList()
)

data class Particle(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color
)
