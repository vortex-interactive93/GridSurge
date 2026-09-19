package com.example.gridsurge.game.model

enum class ScorePopupTier(
    val scaleMultiplier: Float,
    val textColor: Int,
    val glowColor: Int,
    val hasBackingPill: Boolean,
    val durationMs: Long
) {
    STANDARD(1.0f, 0xFF00E5FF.toInt(), 0x8000E5FF.toInt(), false, 750L),
    MULTI_LINE(1.25f, 0xFFFFD600.toInt(), 0x99FFD600.toInt(), true, 900L),
    OVERDRIVE(1.5f, 0xFFFF1744.toInt(), 0xCCFF1744.toInt(), true, 1100L)
}

enum class ScorePopupType {
    STANDARD_POINTS,
    COMBO_MULTIPLIER,
    SURGE_MILESTONE,
    EMP_PURGE
}

class FloatingScoreEntity {
    var x: Float = 0f
    var y: Float = 0f
    var startY: Float = 0f
    var text: String = ""
    var tier: ScorePopupTier = ScorePopupTier.STANDARD
    var startTimeMs: Long = 0L
    var isAlive: Boolean = false
    var floatDistancePx: Float = 0f
    
    // Anti-collision tracking
    var verticalSlotOffset: Float = 0f
}
