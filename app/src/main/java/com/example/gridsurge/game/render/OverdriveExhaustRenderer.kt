package com.example.gridsurge.game.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.gridsurge.game.blitz.model.ExhaustParticle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class OverdriveExhaustRenderer(private val density: Float) {

    private val poolSize = 36
    private val particlePool = Array(poolSize) { ExhaustParticle() }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var isVentingActive = false

    fun triggerExhaustVent(boardRect: RectF) {
        val corners = listOf(
            Pair(boardRect.left, boardRect.top) to -2.356f,      // Top-Left (135 deg)
            Pair(boardRect.right, boardRect.top) to -0.785f,     // Top-Right (45 deg)
            Pair(boardRect.right, boardRect.bottom) to 0.785f,   // Bottom-Right (315 deg)
            Pair(boardRect.left, boardRect.bottom) to 2.356f     // Bottom-Left (225 deg)
        )

        var idx = 0
        corners.forEach { (pos, baseAngle) ->
            for (i in 0 until (poolSize / 4)) {
                if (idx >= poolSize) break
                val p = particlePool[idx++]
                p.x = pos.first
                p.y = pos.second
                val spread = (Random.nextFloat() - 0.5f) * 0.9f
                val angle = baseAngle + spread
                val speed = (180f + Random.nextFloat() * 220f) * density
                p.vx = cos(angle.toDouble()).toFloat() * speed
                p.vy = sin(angle.toDouble()).toFloat() * speed
                p.alpha = 1.0f
                p.size = (3f + Random.nextFloat() * 3.5f) * density
                p.color = if (Random.nextBoolean()) Color.parseColor("#00E5FF") else Color.WHITE
                p.active = true
            }
        }
        isVentingActive = true
    }

    fun updateAndRender(canvas: Canvas, dt: Float) {
        if (!isVentingActive) return
        var anyActive = false

        for (i in 0 until poolSize) {
            val p = particlePool[i]
            if (!p.active) continue

            // Euler kinematic integration with drag
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vx *= (1.0f - 4.5f * dt)
            p.vy *= (1.0f - 4.5f * dt)
            p.alpha -= dt * 2.2f // ~0.45s decay

            if (p.alpha <= 0f) {
                p.active = false
            } else {
                anyActive = true
                particlePaint.color = p.color
                particlePaint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.size * p.alpha, particlePaint)
            }
        }

        isVentingActive = anyActive
    }
}
