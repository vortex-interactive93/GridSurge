package com.example.gridsurge.game.particle

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class CyberParticle {
    var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var sizePx: Float = 0f
    var initialSizePx: Float = 0f
    var alpha: Float = 1f
    var color: Int = Color.CYAN
    var ageMs: Long = 0L
    var maxLifeMs: Long = 500L
    var isSpark: Boolean = false

    fun update(dtSec: Float) {
        if (!isActive) return

        ageMs += (dtSec * 1000f).toLong()
        val progress = ageMs.toFloat() / maxLifeMs.toFloat()

        if (progress >= 1f) {
            isActive = false
            return
        }

        // Physics: Velocity Integration + Aerodynamic Drag + Gentle Gravity
        x += vx * dtSec
        y += vy * dtSec

        vx *= 0.94f // Air resistance
        vy = (vy * 0.94f) + (220f * dtSec) // Gravity downward drift

        // Visual Decay Curves
        alpha = (1f - progress).coerceIn(0f, 1f)
        sizePx = initialSizePx * (1f - progress * 0.6f)
    }
}

class CyberParticleSystem(private val density: Float, maxParticles: Int = 200) {

    private val pool = Array(maxParticles) { CyberParticle() }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val tempRect = RectF()

    val hasActiveParticles: Boolean
        get() = pool.any { it.isActive }

    fun spawnLineExplosion(
        startX: Float,
        startY: Float,
        width: Float,
        height: Float,
        color: Int,
        particleCount: Int = 36
    ) {
        var spawned = 0
        for (p in pool) {
            if (p.isActive) continue

            p.isActive = true
            p.ageMs = 0L
            p.maxLifeMs = Random.nextLong(350L, 550L)
            p.color = color

            // Distribute origin across the cleared cell/line rect
            p.x = startX + Random.nextFloat() * width
            p.y = startY + Random.nextFloat() * height

            // Radial velocity with directional impulse
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = (Random.nextFloat() * 320f + 140f) * density
            p.vx = cos(angle.toDouble()).toFloat() * speed
            p.vy = sin(angle.toDouble()).toFloat() * speed - (80f * density) // Slight upward pop

            p.isSpark = Random.nextFloat() > 0.45f
            p.initialSizePx = if (p.isSpark) (2.5f * density) else (Random.nextFloat() * 4f + 3f) * density
            p.sizePx = p.initialSizePx
            p.alpha = 1f

            spawned++
            if (spawned >= particleCount) break
        }
    }

    fun updateAndDraw(canvas: Canvas, dtSec: Float) {
        for (p in pool) {
            if (!p.isActive) continue

            p.update(dtSec)
            if (!p.isActive) continue

            val drawAlpha = (p.alpha * 255).toInt().coerceIn(0, 255)

            if (p.isSpark) {
                // High-velocity directional neon sparks
                sparkPaint.color = p.color
                sparkPaint.alpha = drawAlpha
                sparkPaint.strokeWidth = p.sizePx

                val tailX = p.x - (p.vx * 0.035f)
                val tailY = p.y - (p.vy * 0.035f)
                canvas.drawLine(tailX, tailY, p.x, p.y, sparkPaint)
            } else {
                // Glowing Cyber Shards
                particlePaint.color = p.color
                particlePaint.alpha = drawAlpha
                val half = p.sizePx / 2f
                tempRect.set(p.x - half, p.y - half, p.x + half, p.y + half)
                canvas.drawRoundRect(tempRect, 2f * density, 2f * density, particlePaint)
            }
        }
    }
}
