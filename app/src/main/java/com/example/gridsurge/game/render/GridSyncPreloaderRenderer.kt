package com.example.gridsurge.game.render

import android.graphics.*
import kotlin.math.sin

class GridSyncPreloaderRenderer(private val density: Float) {

    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#99040812")
    }

    private val chassisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#F008101E")
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = Color.parseColor("#00E5FF")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.MONOSPACE
        textSize = 12f * density
        textAlign = Paint.Align.CENTER
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#556980")
        typeface = Typeface.MONOSPACE
        textSize = 9f * density
        textAlign = Paint.Align.CENTER
    }

    private val bannerBounds = RectF()

    fun drawPreAdNotice(canvas: Canvas, boardRect: RectF, elapsedMs: Long, totalDurationMs: Long = 1200L) {
        if (elapsedMs <= 0L) return

        // 1. Draw Board Scrim
        canvas.drawRect(boardRect, scrimPaint)

        val cx = boardRect.centerX()
        val cy = boardRect.centerY()
        val w = boardRect.width() * 0.82f
        val h = 64f * density

        bannerBounds.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)

        // 2. Draw Beveled Notification Chassis
        canvas.drawRoundRect(bannerBounds, 8f * density, 8f * density, chassisPaint)

        // Pulsing border
        val pulseAlpha = (140 + sin(elapsedMs / 120.0) * 115).toInt().coerceIn(0, 255)
        borderPaint.alpha = pulseAlpha
        canvas.drawRoundRect(bannerBounds, 8f * density, 8f * density, borderPaint)

        // 3. Technical Micro-Copy
        canvas.drawText("TACTICAL BREAK // SYNCING GRID", cx, cy - 4f * density, textPaint)

        val progressPercent = ((elapsedMs.toFloat() / totalDurationMs) * 100).toInt().coerceIn(0, 100)
        canvas.drawText("RESUMING IN MOMENTS • $progressPercent%", cx, cy + 16f * density, subTextPaint)
    }
}
