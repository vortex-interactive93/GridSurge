package com.example.gridsurge.game.render

import android.graphics.*
import com.example.gridsurge.game.glitch.model.ActiveAnomalyNode
import com.example.gridsurge.game.glitch.model.CatalystStatus
import kotlin.math.cos
import kotlin.math.sin

class AnomalyDiodeRenderer(private val density: Float) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.5f * density
    }
    private val countdownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val sigilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }

    private val sigilPath = Path()

    fun renderAnomalyCell(
        canvas: Canvas,
        rect: RectF,
        anomaly: ActiveAnomalyNode,
        now: Long
    ) {
        val pulseSpeed = if (anomaly.status == CatalystStatus.CRITICAL) 55.0 else 120.0
        val pulse = (sin(now / pulseSpeed) * 0.5 + 0.5).toFloat()

        val themeColor = anomaly.status.diodeColor.toInt()

        // 1. Ambient Volumetric Glow
        glowPaint.color = themeColor
        glowPaint.alpha = (70 + pulse * 110).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, glowPaint)

        // 2. Translucent Base Matrix Fill
        fillPaint.color = Color.parseColor("#090E1A")
        canvas.drawRoundRect(rect, 8f * density, 8f * density, fillPaint)

        fillPaint.color = themeColor
        fillPaint.alpha = (25 + pulse * 45).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, fillPaint)

        // 3. Faceted Bio-Hazard Sigil Background
        drawHazardSigil(canvas, rect, themeColor, pulse)

        // 4. Outer Tactical Border
        strokeBorderPaint.color = themeColor
        strokeBorderPaint.alpha = (180 + pulse * 75).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, strokeBorderPaint)

        // 5. Digital Countdown Glyph
        val cx = rect.centerX()
        val cy = rect.centerY()
        countdownPaint.textSize = 15f * density
        countdownPaint.setShadowLayer(4f * density, 0f, 0f, themeColor)
        canvas.drawText("${anomaly.turnsRemaining}", cx, cy + (5f * density), countdownPaint)
    }

    private fun drawHazardSigil(canvas: Canvas, rect: RectF, color: Int, pulse: Float) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val r = rect.width() * 0.32f

        sigilPaint.color = color
        sigilPaint.alpha = (40 + pulse * 60).toInt()

        sigilPath.rewind()
        // Render central hexagon graticule
        for (i in 0..5) {
            val angle = Math.toRadians(60.0 * i)
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) sigilPath.moveTo(x, y) else sigilPath.lineTo(x, y)
        }
        sigilPath.close()
        canvas.drawPath(sigilPath, sigilPaint)

        // Cross-lines
        canvas.drawLine(cx - r * 1.3f, cy, cx + r * 1.3f, cy, sigilPaint)
        canvas.drawLine(cx, cy - r * 1.3f, cx, cy + r * 1.3f, sigilPaint)
    }
}
