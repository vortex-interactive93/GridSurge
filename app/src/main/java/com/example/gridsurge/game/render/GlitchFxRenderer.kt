package com.example.gridsurge.game.render

import android.graphics.*
import androidx.core.graphics.withSave
import com.example.gridsurge.game.glitch.model.AnomalyCellPhase
import com.example.gridsurge.game.glitch.model.GlitchCatalyst
import kotlin.math.sin

class GlitchFxRenderer(private val density: Float) {

    // --- Pre-Allocated Hardware Paints ---
    private val scanlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
        color = Color.parseColor("#1800FF66")
    }

    private val anomalyAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val anomalyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val tempRect = RectF()

    /**
     * Renders active anomaly cells with pulsing core diodes and countdown telemetry.
     */
    fun renderAnomalyCell(
        canvas: Canvas,
        rect: RectF,
        catalyst: GlitchCatalyst,
        now: Long
    ) {
        val pulseSpeed = if (catalyst.phase == AnomalyCellPhase.CRITICAL) 60.0 else 120.0
        val pulse = (sin(now / pulseSpeed) * 0.5 + 0.5).toFloat()

        val coreColor = when (catalyst.phase) {
            AnomalyCellPhase.DORMANT -> Color.parseColor("#00FF66")
            AnomalyCellPhase.UNSTABLE -> Color.parseColor("#FFD600")
            AnomalyCellPhase.CRITICAL -> Color.parseColor("#FF1744")
            AnomalyCellPhase.RUPTURED -> Color.parseColor("#D500F9")
        }

        // 1. Emissive Sub-Layer Fill
        anomalyAuraPaint.color = coreColor
        anomalyAuraPaint.alpha = (40 + pulse * 50).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, anomalyAuraPaint)

        // 2. Neon Warning Perimeter
        anomalyBorderPaint.color = coreColor
        anomalyBorderPaint.alpha = (180 + pulse * 75).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, anomalyBorderPaint)

        // 3. Digital Cross-Hatch Glyphs
        val l = rect.left + 4f * density
        val t = rect.top + 4f * density
        val r = rect.right - 4f * density
        val b = rect.bottom - 4f * density

        canvas.drawLine(l, t, r, b, anomalyBorderPaint)
        canvas.drawLine(l, b, r, t, anomalyBorderPaint)

        // 4. Tactical Countdown Badge
        val cx = rect.centerX()
        val cy = rect.centerY()
        textPaint.textSize = 14f * density
        canvas.drawText("${catalyst.turnsRemaining}", cx, cy + (5f * density), textPaint)
    }

    /**
     * Executes an RGB Split / Chromatic Aberration Screen Tear when Purity drops or during clears.
     */
    fun renderScreenGlitch(
        canvas: Canvas,
        boardRect: RectF,
        purity: Float,
        trauma: Float,
        now: Long
    ) {
        val instability = (1.0f - purity) + trauma
        if (instability <= 0.15f) return

        val tearOffset = (sin(now / 40.0) * instability * 6f * density).toFloat()

        // Draw RGB Chromatic Shift Bars across the board
        canvas.withSave {
            clipRect(boardRect)
            for (i in 0 until 5) {
                val barY = boardRect.top + ((now + i * 140) % boardRect.height().toLong()).toFloat()
                val barH = (4f + instability * 12f) * density
                tempRect.set(boardRect.left, barY, boardRect.right, barY + barH)

                // Cyan Offset Band
                scanlinePaint.color = Color.parseColor("#4400E5FF")
                canvas.drawRect(tempRect.left - tearOffset, tempRect.top, tempRect.right - tearOffset, tempRect.bottom, scanlinePaint)

                // Magenta Offset Band
                scanlinePaint.color = Color.parseColor("#44FF0055")
                canvas.drawRect(tempRect.left + tearOffset, tempRect.top, tempRect.right + tearOffset, tempRect.bottom, scanlinePaint)
            }
        }
    }
}
