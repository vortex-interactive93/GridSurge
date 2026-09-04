package com.example.gridsurge.game.fx

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.sin

class VfxCanvasRenderer {

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }

    private val flareGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val flareCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val waveformPath = Path()

    fun renderEffects(
        canvas: Canvas,
        beams: Array<LaserBeamVfx>,
        flares: Array<LaserBeamVfx>,
        virtualTime: Float
    ) {
        // 1. Render Laser Beams & Waveforms
        for (i in beams.indices) {
            val beam = beams[i]
            if (!beam.isActive || beam.alpha <= 0f) continue

            val currentAlpha = (beam.alpha * 255).toInt().coerceIn(0, 255)
            val glowAlpha = (currentAlpha * 0.6f).toInt().coerceIn(0, 255)

            // Outer Glow
            glowPaint.color = beam.glowColor
            glowPaint.alpha = glowAlpha
            glowPaint.strokeWidth = beam.beamThickness * (1.8f + (1f - beam.progress) * 1.5f)
            canvas.drawLine(beam.startX, beam.startY, beam.endX, beam.endY, glowPaint)

            // Crisp Inner Core
            corePaint.alpha = currentAlpha
            corePaint.strokeWidth = beam.beamThickness * (0.8f + (1f - beam.progress) * 0.4f)
            canvas.drawLine(beam.startX, beam.startY, beam.endX, beam.endY, corePaint)

            // Dynamic Waveform Resonance Sub-Pass
            if (beam.type == VfxType.LASER_HORIZONTAL && beam.alpha > 0.2f) {
                renderWaveform(canvas, beam, virtualTime, currentAlpha)
            }
        }

        // 2. Render Intersection Flares
        for (i in flares.indices) {
            val flare = flares[i]
            if (!flare.isActive || flare.alpha <= 0f) continue

            val currentAlpha = (flare.alpha * 255).toInt().coerceIn(0, 255)
            val radius = flare.beamThickness * (1.0f + (flare.progress * 1.8f))

            flareGlowPaint.color = flare.glowColor
            flareGlowPaint.alpha = (currentAlpha * 0.7f).toInt().coerceIn(0, 255)
            canvas.drawCircle(flare.startX, flare.startY, radius * 1.6f, flareGlowPaint)

            flareCorePaint.alpha = currentAlpha
            canvas.drawCircle(flare.startX, flare.startY, radius * 0.6f, flareCorePaint)
        }
    }

    private fun renderWaveform(
        canvas: Canvas,
        beam: LaserBeamVfx,
        virtualTime: Float,
        alpha: Int
    ) {
        waveformPath.reset()
        val segments = 24
        val length = beam.endX - beam.startX
        val stepX = length / segments
        val midY = beam.startY
        val waveFreq = 0.05f
        val waveSpeed = 18f
        val amplitude = 6f * (beam.alpha)

        waveformPath.moveTo(beam.startX, midY)
        for (s in 1..segments) {
            val px = beam.startX + s * stepX
            val py = midY + sin((px * waveFreq) + (virtualTime * waveSpeed)) * amplitude
            waveformPath.lineTo(px, py.toFloat())
        }

        glowPaint.strokeWidth = 2.0f
        glowPaint.alpha = (alpha * 0.85f).toInt()
        canvas.drawPath(waveformPath, glowPaint)
    }
}
