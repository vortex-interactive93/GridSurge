package com.example.gridsurge.game.render

import android.graphics.*
import com.example.gridsurge.game.model.FloatingScoreEntity
import com.example.gridsurge.game.model.ScorePopupType
import java.util.Locale

class FloatingScoreManager(private val density: Float, maxPopups: Int = 16) {

    private val pool = Array(maxPopups) { FloatingScoreEntity() }

    // Pre-allocated Typography Paints
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    private val textGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val bannerSubTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.WHITE
    }

    val hasActivePopups: Boolean
        get() = pool.any { it.isActive }

    fun spawnScore(
        x: Float,
        y: Float,
        scoreDelta: Long,
        comboStreak: Int,
        now: Long = android.os.SystemClock.uptimeMillis()
    ) {
        val entity = pool.firstOrNull { !it.isActive } ?: pool[0]

        val (primaryText, subText, color, glowColor, type, duration) = when {
            comboStreak >= 7 -> {
                val banner = "UNSTOPPABLE! +${String.format(Locale.US, "%,d", scoreDelta)}"
                val sub = "${comboStreak}x OVERDRIVE CHAIN"
                Tuple6(banner, sub, Color.WHITE, Color.parseColor("#FF0055"), ScorePopupType.SURGE_MILESTONE, 850L)
            }
            comboStreak >= 5 -> {
                val banner = "MEGA BLITZ! +${String.format(Locale.US, "%,d", scoreDelta)}"
                val sub = "${comboStreak}x SURGE COMBO"
                Tuple6(banner, sub, Color.WHITE, Color.parseColor("#FFD600"), ScorePopupType.SURGE_MILESTONE, 800L)
            }
            comboStreak in 2..4 -> {
                val text = "+${String.format(Locale.US, "%,d", scoreDelta)}"
                val sub = "COMBO x$comboStreak"
                Tuple6(text, sub, Color.parseColor("#00E5FF"), Color.parseColor("#007A8C"), ScorePopupType.COMBO_MULTIPLIER, 700L)
            }
            else -> {
                val text = "+${String.format(Locale.US, "%,d", scoreDelta)}"
                Tuple6(text, null, Color.parseColor("#00E5FF"), Color.parseColor("#004D5A"), ScorePopupType.STANDARD_POINTS, 600L)
            }
        }

        entity.spawn(
            x = x,
            y = y,
            primaryText = primaryText,
            subText = subText,
            color = color,
            glowColor = glowColor,
            type = type,
            driftPx = 48f * density,
            duration = duration,
            now = now
        )
    }

    fun spawnPopup(
        x: Float,
        y: Float,
        text: String,
        color: Int,
        now: Long = android.os.SystemClock.uptimeMillis()
    ) {
        val entity = pool.firstOrNull { !it.isActive } ?: pool[0]
        entity.spawn(
            x = x,
            y = y,
            primaryText = text,
            subText = null,
            color = color,
            glowColor = color,
            type = ScorePopupType.STANDARD_POINTS,
            driftPx = 54f * density,
            duration = 750L,
            now = now
        )
    }

    fun clearAll() {
        pool.forEach { it.isActive = false }
    }

    fun render(canvas: Canvas, now: Long) {
        for (entity in pool) {
            if (!entity.isActive) continue

            val isAlive = entity.update(now)
            if (!isAlive) continue

            val baseTextSize = when (entity.type) {
                ScorePopupType.SURGE_MILESTONE -> 20f * density
                ScorePopupType.COMBO_MULTIPLIER -> 18f * density
                else -> 15f * density
            }

            canvas.save()
            canvas.translate(entity.x, entity.y)
            canvas.scale(entity.scale, entity.scale)

            val drawAlpha = (entity.alpha * 255).toInt().coerceIn(0, 255)

            // 1. Neon Outer Glow Stroke Pass
            textGlowPaint.textSize = baseTextSize
            textGlowPaint.strokeWidth = 3.5f * density
            textGlowPaint.color = entity.glowColor
            textGlowPaint.alpha = drawAlpha
            canvas.drawText(entity.primaryText, 0f, 0f, textGlowPaint)

            // 2. High-Contrast Core Text Pass
            textPaint.textSize = baseTextSize
            textPaint.color = entity.primaryColor
            textPaint.alpha = drawAlpha
            canvas.drawText(entity.primaryText, 0f, 0f, textPaint)

            // 3. Subtext Banner Pass (for Combos / Milestones)
            if (entity.subText != null) {
                bannerSubTextPaint.textSize = 10f * density
                bannerSubTextPaint.alpha = (drawAlpha * 0.9f).toInt()
                canvas.drawText(entity.subText!!, 0f, 13f * density, bannerSubTextPaint)
            }

            canvas.restore()
        }
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
    )
}
