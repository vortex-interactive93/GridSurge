package com.example.gridsurge.game.fx

import android.graphics.*
import kotlin.random.Random

class OverdriveChassisFx(private val density: Float) {

    private val arcPath = Path()

    // Pre-allocated Paints (Zero allocations in onDraw)
    private val plasmaGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#00E5FF")
    }
    private val plasmaCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }
    private val nodeOrbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFF3B0")
    }

    private var lastArcGenTime = 0L
    private var decayAlpha = 0f

    fun renderOverdriveChassis(
        canvas: Canvas,
        gridBounds: RectF,
        comboStreak: Int,
        now: Long
    ) {
        if (comboStreak >= 4) {
            decayAlpha = 1.0f
        } else {
            decayAlpha = (decayAlpha - 0.05f).coerceAtLeast(0f)
        }
        
        if (decayAlpha <= 0f) return

        val effectiveStreak = if (comboStreak >= 4) comboStreak else 4
        val intensity = ((effectiveStreak - 3) / 4f).coerceIn(0.2f, 1.0f)
        val isMaxOverdrive = effectiveStreak >= 7

        // 1. Color modulation based on overdrive tier
        val glowColor = when {
            isMaxOverdrive -> Color.parseColor("#FF0055") // Crimson Singularity (7x+)
            effectiveStreak >= 5 -> Color.parseColor("#FFD600") // Solar Gold (5x-6x)
            else -> Color.parseColor("#00E5FF")             // Cyan Surge (4x)
        }

        plasmaGlowPaint.color = glowColor
        plasmaGlowPaint.strokeWidth = (6f * density) * (0.8f + intensity * 0.6f)
        plasmaGlowPaint.alpha = ((160 + (intensity * 95)) * decayAlpha).toInt()

        plasmaCorePaint.strokeWidth = 2f * density
        plasmaCorePaint.alpha = (255 * decayAlpha).toInt()

        // 2. Generate procedural perimeter lightning path (every 45ms)
        if (now - lastArcGenTime > 45L) {
            lastArcGenTime = now
            buildPerimeterLightningPath(gridBounds, intensity)
        }

        // 3. Draw Dual-Pass Plasma Lightning
        canvas.drawPath(arcPath, plasmaGlowPaint)
        canvas.drawPath(arcPath, plasmaCorePaint)

        // 4. Render Corner Resonator Nodes
        val cornerRadius = 6f * density * (1f + intensity * 0.4f)
        val corners = arrayOf(
            PointF(gridBounds.left, gridBounds.top),
            PointF(gridBounds.right, gridBounds.top),
            PointF(gridBounds.right, gridBounds.bottom),
            PointF(gridBounds.left, gridBounds.bottom)
        )

        corners.forEach { pt ->
            nodeOrbPaint.color = glowColor
            nodeOrbPaint.alpha = ((200 + (intensity * 55)) * decayAlpha).toInt()
            canvas.drawCircle(pt.x, pt.y, cornerRadius * 1.5f, plasmaGlowPaint)
            nodeOrbPaint.color = Color.WHITE
            nodeOrbPaint.alpha = (255 * decayAlpha).toInt()
            canvas.drawCircle(pt.x, pt.y, cornerRadius * 0.7f, nodeOrbPaint)
        }
    }

    private fun buildPerimeterLightningPath(bounds: RectF, intensity: Float) {
        arcPath.reset()
        val jitterMax = (4.5f * density) * intensity

        // Top Edge
        generateSegment(bounds.left, bounds.top, bounds.right, bounds.top, jitterMax, isHorizontal = true)
        // Right Edge
        generateSegment(bounds.right, bounds.top, bounds.right, bounds.bottom, jitterMax, isHorizontal = false)
        // Bottom Edge
        generateSegment(bounds.right, bounds.bottom, bounds.left, bounds.bottom, jitterMax, isHorizontal = true)
        // Left Edge
        generateSegment(bounds.left, bounds.bottom, bounds.left, bounds.top, jitterMax, isHorizontal = false)
    }

    private fun generateSegment(
        x1: Float, y1: Float, x2: Float, y2: Float,
        jitter: Float, isHorizontal: Boolean
    ) {
        val steps = 6

        if (arcPath.isEmpty) {
            arcPath.moveTo(x1, y1)
        }

        for (i in 1..steps) {
            val t = i / steps.toFloat()
            var nextX = x1 + (x2 - x1) * t
            var nextY = y1 + (y2 - y1) * t

            if (i < steps) {
                if (isHorizontal) {
                    nextY += (Random.nextFloat() * 2f - 1f) * jitter
                } else {
                    nextX += (Random.nextFloat() * 2f - 1f) * jitter
                }
            }

            arcPath.lineTo(nextX, nextY)
        }
    }
}
