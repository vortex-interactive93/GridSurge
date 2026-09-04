package com.example.gridsurge.game.render

import android.graphics.*
import com.example.gridsurge.game.blitz.BlitzState
import com.example.gridsurge.game.blitz.TimeBlitzEngine
import kotlin.math.sin
import kotlin.random.Random

class TimeBlitzHudRenderer(private val density: Float) {

    private val timerBarBounds = RectF()
    private val feverBarBounds = RectF()
    private val feverProgressBounds = RectF()
    private val timerProgressBounds = RectF()
    private val feverBadgeRect = RectF()
    private val auraPath = Path()

    // Pre-allocated Paints (Zero allocations in onDraw)
    private val gaugeTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#99040711")
    }
    private val gaugeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = Color.parseColor("#263859")
    }
    private val timerFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val feverFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val textHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    private val feverBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val feverBadgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E6FF0055")
    }

    private val auraGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val auraCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }

    private var lastAuraTime = 0L

    fun renderBlitzHud(
        canvas: Canvas,
        gridBounds: RectF,
        blitzEngine: TimeBlitzEngine,
        now: Long
    ) {
        val hudTop = gridBounds.top - (54f * density)
        timerBarBounds.set(gridBounds.left, hudTop, gridBounds.right, hudTop + (11f * density))
        feverBarBounds.set(gridBounds.left, timerBarBounds.bottom + (5f * density), gridBounds.right, timerBarBounds.bottom + (11f * density))

        val isFever = (blitzEngine.state == BlitzState.FEVER_ACTIVE)
        val timeSec = blitzEngine.secondsRemaining
        val timeRatio = (timeSec / blitzEngine.initialTimeSec).coerceIn(0f, 1f)
        val isLowTime = timeSec <= 15f && blitzEngine.state == BlitzState.RUNNING

        // 1. Draw Time Gauge
        canvas.drawRoundRect(timerBarBounds, 3f * density, 3f * density, gaugeTrackPaint)
        canvas.drawRoundRect(timerBarBounds, 3f * density, 3f * density, gaugeBorderPaint)

        timerFillPaint.color = when {
            isLowTime -> {
                val pulse = (sin(now / 80.0) * 0.5 + 0.5).toFloat()
                if (pulse > 0.5f) Color.parseColor("#FF0055") else Color.parseColor("#80FF0055")
            }
            timeSec <= 30f -> Color.parseColor("#FFD600")
            else -> Color.parseColor("#00E5FF")
        }

        timerProgressBounds.set(
            timerBarBounds.left,
            timerBarBounds.top,
            timerBarBounds.left + (timerBarBounds.width() * timeRatio),
            timerBarBounds.bottom
        )
        canvas.drawRoundRect(timerProgressBounds, 3f * density, 3f * density, timerFillPaint)

        // 2. Draw Fever Gauge
        canvas.drawRoundRect(feverBarBounds, 3f * density, 3f * density, gaugeTrackPaint)
        canvas.drawRoundRect(feverBarBounds, 3f * density, 3f * density, gaugeBorderPaint)

        val feverRatio = blitzEngine.feverMeter.coerceIn(0f, 1f)

        feverFillPaint.color = if (isFever) {
            val hue = (now % 1200L) / 1200f * 360f // Cycling Rainbow Plasma
            Color.HSVToColor(floatArrayOf(hue, 0.9f, 1.0f))
        } else {
            Color.parseColor("#FF0055")
        }

        feverProgressBounds.set(
            feverBarBounds.left,
            feverBarBounds.top,
            feverBarBounds.left + (feverBarBounds.width() * feverRatio),
            feverBarBounds.bottom
        )
        canvas.drawRoundRect(feverProgressBounds, 3f * density, 3f * density, feverFillPaint)

        // 3. Header & Non-Colliding Fever Banner
        textHeaderPaint.textSize = 10f * density
        textHeaderPaint.color = Color.parseColor("#8FA3BF")
        canvas.drawText("QUANTUM TIME BLITZ", timerBarBounds.left, hudTop - (6f * density), textHeaderPaint)

        if (isFever) {
            val bannerW = 210f * density
            val bannerH = 18f * density
            val bannerCx = gridBounds.centerX()
            val bannerCy = hudTop - (10f * density)

            feverBadgeRect.set(bannerCx - bannerW / 2f, bannerCy - bannerH / 2f, bannerCx + bannerW / 2f, bannerCy + bannerH / 2f)
            
            // Draw subtle glow badge behind text
            val pulse = (sin(now / 100.0) * 0.5 + 0.5).toFloat()
            feverBadgeBgPaint.color = Color.HSVToColor((180 + pulse * 75).toInt(), floatArrayOf((now % 1000L) / 1000f * 360f, 0.9f, 0.9f))
            canvas.drawRoundRect(feverBadgeRect, 4f * density, 4f * density, feverBadgeBgPaint)

            feverBadgePaint.textSize = 10.5f * density
            feverBadgePaint.color = Color.WHITE
            canvas.drawText("★ QUANTUM FEVER 2X OVERDRIVE ★", bannerCx, bannerCy + (3.5f * density), feverBadgePaint)

            renderQuantumEventHorizonAura(canvas, gridBounds, now)
        }
    }

    private fun renderQuantumEventHorizonAura(canvas: Canvas, bounds: RectF, now: Long) {
        val hue = ((now + 300) % 1400L) / 1400f * 360f
        val auraColor = Color.HSVToColor(floatArrayOf(hue, 0.85f, 1.0f))

        auraGlowPaint.color = auraColor
        auraGlowPaint.strokeWidth = 6f * density
        auraGlowPaint.alpha = (180 + sin(now / 55.0) * 75).toInt()

        auraCorePaint.strokeWidth = 2.5f * density
        auraCorePaint.alpha = 255

        if (now - lastAuraTime > 35L) {
            lastAuraTime = now
            buildAuraPath(bounds)
        }

        canvas.drawPath(auraPath, auraGlowPaint)
        canvas.drawPath(auraPath, auraCorePaint)
    }

    private fun buildAuraPath(bounds: RectF) {
        auraPath.reset()
        val jitter = 4.5f * density
        traceEdge(bounds.left, bounds.top, bounds.right, bounds.top, jitter, isHoriz = true)
        traceEdge(bounds.right, bounds.top, bounds.right, bounds.bottom, jitter, isHoriz = false)
        traceEdge(bounds.right, bounds.bottom, bounds.left, bounds.bottom, jitter, isHoriz = true)
        traceEdge(bounds.left, bounds.bottom, bounds.left, bounds.top, jitter, isHoriz = false)
    }

    private fun traceEdge(x1: Float, y1: Float, x2: Float, y2: Float, jitter: Float, isHoriz: Boolean) {
        val steps = 8
        if (auraPath.isEmpty) auraPath.moveTo(x1, y1)
        for (i in 1..steps) {
            val t = i / steps.toFloat()
            var nx = x1 + (x2 - x1) * t
            var ny = y1 + (y2 - y1) * t
            if (i < steps) {
                if (isHoriz) ny += (Random.nextFloat() * 2f - 1f) * jitter
                else nx += (Random.nextFloat() * 2f - 1f) * jitter
            }
            auraPath.lineTo(nx, ny)
        }
    }
}
