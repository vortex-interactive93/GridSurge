package com.example.gridsurge.game.render

import android.graphics.*
import com.example.gridsurge.game.blitz.model.BlitzTerminalPhase
import com.example.gridsurge.game.blitz.model.BlitzTerminalSequenceState

class BlitzFinaleOverlayRenderer(private val density: Float) {

    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#9902050A")
    }

    private val bannerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#F5080E18")
    }

    private val bannerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.dpToPx()
        color = Color.parseColor("#FFFF1744")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6.dpToPx()
        color = Color.parseColor("#FFFF1744")
        maskFilter = BlurMaskFilter(5.dpToPx(), BlurMaskFilter.Blur.NORMAL)
    }

    private val bannerRect = RectF()
    private val fontMetrics = Paint.FontMetrics()

    fun renderFinaleSequence(
        canvas: Canvas,
        boardRect: RectF,
        sequenceState: BlitzTerminalSequenceState
    ) {
        if (sequenceState.phase == BlitzTerminalPhase.RUNNING || sequenceState.phase == BlitzTerminalPhase.DEBRIEF_MOUNTED) return

        // 1. Stasis Screen Scrim
        canvas.drawRect(boardRect, scrimPaint)

        // 2. Compute Kinetic Scale
        val t = (sequenceState.sequenceElapsedSec / sequenceState.freezeDurationSec).coerceIn(0f, 1f)
        val scale = when {
            t < 0.25f -> (t / 0.25f) * 1.08f // Slight overshoot pop
            t < 0.40f -> 1.08f - ((t - 0.25f) / 0.15f) * 0.08f // Snap to 1.0
            else -> 1.0f
        }

        val cx = boardRect.centerX()
        val cy = boardRect.centerY()
        val bannerW = boardRect.width() * 0.88f
        val bannerH = 54.dpToPx()

        bannerRect.set(cx - bannerW / 2f, cy - bannerH / 2f, cx + bannerW / 2f, cy + bannerH / 2f)

        val saveCount = canvas.save()
        canvas.scale(scale, scale, cx, cy)

        // 3. Emissive Outer Border Glow
        canvas.drawRoundRect(bannerRect, 10.dpToPx(), 10.dpToPx(), glowPaint)

        // 4. Solid Chassis Fill & Crisp Border
        canvas.drawRoundRect(bannerRect, 10.dpToPx(), 10.dpToPx(), bannerBgPaint)
        canvas.drawRoundRect(bannerRect, 10.dpToPx(), 10.dpToPx(), bannerBorderPaint)

        // 5. "TIME'S UP!" High-Impact Typography
        textPaint.textSize = 22.dpToPx()
        textPaint.color = Color.parseColor("#FFFFD600")
        textPaint.getFontMetrics(fontMetrics)
        val baseline = bannerRect.centerY() + (fontMetrics.descent - fontMetrics.ascent) / 2f - fontMetrics.descent
        canvas.drawText("TIME'S UP!", cx, baseline, textPaint)

        canvas.restoreToCount(saveCount)
    }

    private fun Int.dpToPx(): Float = this * density
}
