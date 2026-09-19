package com.example.gridsurge.game.render

import android.graphics.*
import android.os.SystemClock
import com.example.gridsurge.game.model.FloatingScoreEntity
import com.example.gridsurge.game.model.ScorePopupTier
import kotlin.math.abs
import kotlin.math.sin

class FloatingScoreManager(
    private val density: Float,
    private val maxPopups: Int = 16
) {
    private val pool = Array(maxPopups) { FloatingScoreEntity() }
    
    // --- Pre-Allocated Hardware Paints (Zero GC Churn) ---
    private val textStrokeHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5.5f * density
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#060A14")
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val textFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val pillBackplatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E6050A16") // Deep translucent cyber scrim
    }

    private val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }

    private val tempPillRect = RectF()
    private val tempTextBounds = Rect()
    private val floatDistancePx = 54f * density

    val hasActivePopups: Boolean
        get() = pool.any { it.isAlive }

    /**
     * Spawns a score popup with automatic tier categorization and anti-stacking offset.
     */
    fun spawnScore(
        originX: Float,
        originY: Float,
        points: Long,
        isCombo: Boolean = false,
        streakCount: Int = 1,
        now: Long = SystemClock.uptimeMillis()
    ) {
        val tier = when {
            points >= 1200L || streakCount >= 4 -> ScorePopupTier.OVERDRIVE
            points >= 400L || streakCount >= 2 -> ScorePopupTier.MULTI_LINE
            else -> ScorePopupTier.STANDARD
        }

        val formatted = when {
            tier == ScorePopupTier.OVERDRIVE -> "[ +$points SURGE // OVERDRIVE ]"
            tier == ScorePopupTier.MULTI_LINE -> "[ +$points SURGE // DUAL CLEAR ]"
            isCombo -> "[ +$points SURGE // ${streakCount}x COMBO ]"
            else -> "[ +$points ]"
        }

        spawnPopup(originX, originY, formatted, tier, now)
    }

    /**
     * Raw spawn API compatible with existing engine callers.
     */
    fun spawnPopup(
        originX: Float,
        originY: Float,
        text: String,
        color: Int,
        now: Long = SystemClock.uptimeMillis()
    ) {
        val tier = when (color) {
            Color.RED, 0xFFFF1744.toInt() -> ScorePopupTier.OVERDRIVE
            Color.YELLOW, 0xFFFFD600.toInt() -> ScorePopupTier.MULTI_LINE
            else -> ScorePopupTier.STANDARD
        }
        spawnPopup(originX, originY, text, tier, now)
    }

    fun spawnPopup(
        originX: Float,
        originY: Float,
        text: String,
        tier: ScorePopupTier,
        now: Long = SystemClock.uptimeMillis()
    ) {
        // Find first available slot in pool
        val entity = pool.firstOrNull { !it.isAlive } 
            ?: pool.minByOrNull { it.startTimeMs } 
            ?: return

        // Anti-Collision Stacking: Calculate vertical offset based on living popups near the same Y
        var slotOffset = 0f
        val activeCountNearOrigin = pool.count { it.isAlive && abs(it.y - originY) < 32f * density }
        if (activeCountNearOrigin > 0) {
            slotOffset = -(activeCountNearOrigin * 22f * density)
        }

        entity.apply {
            this.x = originX
            this.y = originY
            this.startY = originY
            this.text = text
            this.tier = tier
            this.startTimeMs = now
            this.floatDistancePx = this@FloatingScoreManager.floatDistancePx
            this.verticalSlotOffset = slotOffset
            this.isAlive = true
        }
    }

    fun render(canvas: Canvas, now: Long) {
        for (i in 0 until maxPopups) {
            val p = pool[i]
            if (!p.isAlive) continue

            val elapsed = now - p.startTimeMs
            if (elapsed >= p.tier.durationMs) {
                p.isAlive = false
                continue
            }

            val progress = (elapsed.toFloat() / p.tier.durationMs).coerceIn(0f, 1f)

            // 1. Kinetic Scale Calculation
            val punchScale = when {
                progress <= 0.15f -> {
                    val t = progress / 0.15f
                    0.6f + 0.75f * sin(t * (Math.PI / 2.0).toFloat())
                }
                progress <= 0.35f -> {
                    val t = (progress - 0.15f) / 0.20f
                    1.35f - 0.35f * sin(t * (Math.PI / 2.0).toFloat())
                }
                else -> 1.0f
            } * p.tier.scaleMultiplier

            // 2. Trajectory & Alpha Calculations
            val easeOutY = 1.0f - (1.0f - progress) * (1.0f - progress)
            val currentY = p.startY + p.verticalSlotOffset - (p.floatDistancePx * easeOutY)

            val alpha = when {
                progress > 0.70f -> {
                    val fadeT = (1.0f - progress) / 0.30f
                    (fadeT * fadeT * 255f).toInt().coerceIn(0, 255)
                }
                else -> 255
            }

            val baseTextSize = 16f * density * punchScale
            textStrokeHaloPaint.textSize = baseTextSize
            textFillPaint.textSize = baseTextSize
            textFillPaint.color = p.tier.textColor

            val saveCount = canvas.save()
            canvas.translate(p.x, currentY)

            textFillPaint.getTextBounds(p.text, 0, p.text.length, tempTextBounds)

            // 3. Render Tactical Cyber Scrim Pill for Multi-Line and Overdrive
            if (p.tier.hasBackingPill) {
                val padX = 10f * density
                val padY = 5f * density
                
                tempPillRect.set(
                    -tempTextBounds.width() / 2f - padX,
                    -tempTextBounds.height() / 2f - padY,
                    tempTextBounds.width() / 2f + padX,
                    tempTextBounds.height() / 2f + padY
                )

                pillBackplatePaint.alpha = (alpha * 0.88f).toInt()
                pillBorderPaint.color = p.tier.textColor
                pillBorderPaint.alpha = (alpha * 0.75f).toInt()

                canvas.drawRoundRect(tempPillRect, 6f * density, 6f * density, pillBackplatePaint)
                canvas.drawRoundRect(tempPillRect, 6f * density, 6f * density, pillBorderPaint)
            }

            // 4. Dual-Pass Text Halo Pass
            textStrokeHaloPaint.alpha = (alpha * 0.95f).toInt()
            canvas.drawText(p.text, 0f, (tempTextBounds.height() / 2f).coerceAtLeast(0f), textStrokeHaloPaint)

            // 5. High-Luminance Foreground Core Pass
            textFillPaint.alpha = alpha
            canvas.drawText(p.text, 0f, (tempTextBounds.height() / 2f).coerceAtLeast(0f), textFillPaint)

            canvas.restoreToCount(saveCount)
        }
    }

    fun clearAll() {
        for (i in 0 until maxPopups) {
            pool[i].isAlive = false
        }
    }
}
