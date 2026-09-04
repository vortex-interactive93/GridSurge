package com.example.gridsurge.game.fx

import android.graphics.*

class WarpVortexFxEngine(density: Float) {

    private data class InwardParticle(
        var currentX: Float,
        var currentY: Float,
        val startX: Float,
        val startY: Float,
        val targetX: Float,
        val targetY: Float,
        val color: Int,
        val durationMs: Long,
        val startTimeMs: Long
    )

    private val activeParticles = mutableListOf<InwardParticle>()
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    fun triggerWarpImplosion(
        targetX: Float,
        targetY: Float,
        sourceTiles: List<Triple<Float, Float, Int>>,
        now: Long = android.os.SystemClock.uptimeMillis()
    ) {
        if (sourceTiles.isEmpty()) return

        sourceTiles.forEach { (sx, sy, color) ->
            val safeColor = if (color != 0 && color != -1) color else Color.parseColor("#00E5FF")
            activeParticles.add(
                InwardParticle(
                    currentX = sx,
                    currentY = sy,
                    startX = sx,
                    startY = sy,
                    targetX = targetX,
                    targetY = targetY,
                    color = safeColor,
                    durationMs = 450L,
                    startTimeMs = now
                )
            )
        }
    }

    fun hasActiveVortices(): Boolean = activeParticles.isNotEmpty()

    fun clearAll() {
        activeParticles.clear()
    }

    fun render(canvas: Canvas, now: Long, cellSize: Float) {
        if (activeParticles.isEmpty()) return

        val iterator = activeParticles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            val elapsed = now - p.startTimeMs
            val progress = (elapsed.toFloat() / p.durationMs.toFloat()).coerceIn(0f, 1f)

            if (progress >= 1.0f) {
                iterator.remove()
                continue
            }

            // Inward spiral interpolation
            val easeProgress = progress * progress
            p.currentX = p.startX + (p.targetX - p.startX) * easeProgress
            p.currentY = p.startY + (p.targetY - p.startY) * easeProgress

            particlePaint.color = p.color
            particlePaint.alpha = ((1f - progress) * 255).toInt().coerceIn(0, 255)
            val size = (cellSize * 0.45f) * (1f - progress * 0.5f)
            canvas.drawCircle(p.currentX, p.currentY, size / 2f, particlePaint)
        }
    }
}
