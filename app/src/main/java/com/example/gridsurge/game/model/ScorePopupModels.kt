package com.example.gridsurge.game.model

import android.graphics.Color

enum class ScorePopupType {
    STANDARD_POINTS,   // e.g., "+120", "+360"
    COMBO_MULTIPLIER,  // e.g., "COMBO x3 (+900)"
    SURGE_MILESTONE,   // e.g., "OVERDRIVE! +2,400", "MEGA BLITZ! +5,000"
    EMP_PURGE          // e.g., "VIRUS PURGED! +1,500"
}

class FloatingScoreEntity {
    var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var startY: Float = 0f
    var driftDistancePx: Float = 0f
    var startTimeMs: Long = 0L
    var durationMs: Long = 650L

    var primaryText: String = ""
    var subText: String? = null
    var primaryColor: Int = Color.CYAN
    var glowColor: Int = Color.CYAN
    var type: ScorePopupType = ScorePopupType.STANDARD_POINTS
    var scale: Float = 1f
    var alpha: Float = 1f

    fun spawn(
        x: Float,
        y: Float,
        primaryText: String,
        subText: String?,
        color: Int,
        glowColor: Int,
        type: ScorePopupType,
        driftPx: Float,
        duration: Long,
        now: Long
    ) {
        this.isActive = true
        this.x = x
        this.y = y
        this.startY = y
        this.driftDistancePx = driftPx
        this.primaryText = primaryText
        this.subText = subText
        this.primaryColor = color
        this.glowColor = glowColor
        this.type = type
        this.durationMs = duration
        this.startTimeMs = now
        this.scale = 1f
        this.alpha = 1f
    }

    fun update(now: Long): Boolean {
        if (!isActive) return false
        val elapsed = now - startTimeMs
        val progress = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

        if (progress >= 1f) {
            isActive = false
            return false
        }

        // Elastic overshoot on spawn -> float up -> settle & fade
        scale = when {
            progress < 0.20f -> {
                val p = progress / 0.20f
                0.6f + (0.75f * kotlin.math.sin(p * Math.PI.toFloat() / 2f)) // 0.6 -> 1.35x
            }
            progress < 0.40f -> {
                val p = (progress - 0.20f) / 0.20f
                1.35f - (0.35f * p) // 1.35x -> 1.0x
            }
            else -> 1.0f
        }

        // Non-linear upward decelerating drift
        val driftProgress = kotlin.math.sin(progress * Math.PI.toFloat() / 2f)
        y = startY - (driftDistancePx * driftProgress)

        // Alpha fade-out curve in the final 35% of life
        alpha = if (progress > 0.65f) {
            1f - ((progress - 0.65f) / 0.35f)
        } else {
            1f
        }.coerceIn(0f, 1f)

        return true
    }
}
