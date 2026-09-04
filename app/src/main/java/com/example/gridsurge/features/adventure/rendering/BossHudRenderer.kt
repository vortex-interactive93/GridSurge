package com.example.gridsurge.features.adventure.rendering

import android.graphics.*
import androidx.core.graphics.toColorInt
import com.example.gridsurge.features.adventure.model.BossBattleState
import com.example.gridsurge.features.adventure.model.BossPhase
import kotlin.math.sin

class BossHudRenderer(private val density: Float) {

    private val barBounds = RectF()
    private val fillBounds = RectF()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#CC080D1A".toColorInt()
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
        textSize = 10f * density
        color = Color.WHITE
    }
    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
        textSize = 9f * density
    }
    private val pipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pipPath = Path()

    fun renderBossHud(canvas: Canvas, anchorRect: RectF, bossState: BossBattleState, now: Long) {
        if (bossState.phase == BossPhase.DEFEATED) return

        val hudHeight = 22f * density
        // Moved into a dedicated row 12dp below the visor/combo area
        val hudTop = 12f * density 
        barBounds.set(anchorRect.left, hudTop, anchorRect.right, hudTop + hudHeight)

        // 1. Background Console Bar
        canvas.drawRoundRect(barBounds, 6f * density, 6f * density, bgPaint)

        // 2. Health / Shield Fill
        val hpRatio = (bossState.currentHp / bossState.maxHp.toFloat()).coerceIn(0f, 1f)
        val fillWidth = (barBounds.width() - (4f * density)) * hpRatio
        fillBounds.set(barBounds.left + (2f * density), barBounds.top + (2f * density), barBounds.left + (2f * density) + fillWidth, barBounds.bottom - (2f * density))

        if (bossState.phase == BossPhase.SHIELDED) {
            val pulse = (sin(now / 120.0) * 0.5 + 0.5).toFloat()
            borderPaint.color = Color.parseColor("#00E5FF")
            fillPaint.color = Color.parseColor("#00B0FF")
            fillPaint.alpha = (160 + pulse * 95).toInt()
            statusPaint.color = Color.parseColor("#00E5FF")

            canvas.drawRoundRect(fillBounds, 4f * density, 4f * density, fillPaint)
            canvas.drawRoundRect(barBounds, 6f * density, 6f * density, borderPaint)

            canvas.drawText("${bossState.bossName} // SHIELDED", barBounds.left + (8f * density), barBounds.centerY() + (3.5f * density), textPaint)
            
            val rightLabel = "PYLONS:"
            val labelX = barBounds.right - (75f * density)
            canvas.drawText(rightLabel, labelX, barBounds.centerY() + (3.5f * density), statusPaint)

            val pylonIndices = listOf(9, 14, 49, 54)
            val pylonStartX = labelX + statusPaint.measureText(rightLabel) + (4f * density)

            pylonIndices.forEachIndexed { i, idx ->
                val isAlive = idx in bossState.shieldPylonIndices
                val pipX = pylonStartX + (i * 9f * density)
                val pipY = barBounds.centerY()

                pipPath.reset()
                pipPath.moveTo(pipX, pipY - 3.5f * density)
                pipPath.lineTo(pipX + 3.5f * density, pipY)
                pipPath.lineTo(pipX, pipY + 3.5f * density)
                pipPath.lineTo(pipX - 3.5f * density, pipY)
                pipPath.close()

                pipPaint.color = if (isAlive) Color.parseColor("#00E5FF") else Color.parseColor("#3300E5FF")
                pipPaint.style = Paint.Style.FILL
                canvas.drawPath(pipPath, pipPaint)

                if (isAlive) {
                    pipPaint.color = Color.WHITE
                    pipPaint.style = Paint.Style.STROKE
                    pipPaint.strokeWidth = 1f * density
                    canvas.drawPath(pipPath, pipPaint)
                }
            }
        } else {
            // Overdrive Vulnerable (Molten Gold / Crimson)
            val pulse = (sin(now / 70.0) * 0.5 + 0.5).toFloat()
            borderPaint.color = Color.parseColor("#FFD600")
            fillPaint.color = Color.parseColor("#FF0055")
            fillPaint.alpha = (180 + pulse * 75).toInt()
            statusPaint.color = Color.parseColor("#FF0055") // Flashing Crimson

            canvas.drawRoundRect(fillBounds, 4f * density, 4f * density, fillPaint)
            canvas.drawRoundRect(barBounds, 6f * density, 6f * density, borderPaint)

            canvas.drawText("CORE VULNERABLE // STRIKE CENTER", barBounds.left + (8f * density), barBounds.centerY() + (3.5f * density), textPaint)
            val hitsRemaining = (bossState.currentHp / 10).toInt()
            canvas.drawText("BOSS INTEGRITY: [ ${2 - hitsRemaining} / 2 HITS ]", barBounds.right - (8f * density), barBounds.centerY() + (3.5f * density), statusPaint)
        }
    }
}
