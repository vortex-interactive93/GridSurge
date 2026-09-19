package com.example.gridsurge.game.render

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.pow
import kotlin.random.Random

class ClashAttackProjectile {
    var startX = 0f
    var startY = 0f
    var targetX = 0f
    var targetY = 0f
    var controlX = 0f
    var controlY = 0f
    var progress = 0f
    var active = false
    var color = Color.CYAN
    var size = 6f
}

class ClashAttackEmitterRenderer(private val density: Float) {

    private val poolSize = 24
    private val pool = Array(poolSize) { ClashAttackProjectile() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(4f * density, BlurMaskFilter.Blur.NORMAL)
    }

    fun spawnAttackVolley(originX: Float, originY: Float, targetX: Float, targetY: Float, lineCount: Int) {
        val count = (lineCount * 3).coerceIn(3, 12)
        var spawned = 0

        for (i in 0 until poolSize) {
            if (spawned >= count) break
            val p = pool[i]
            if (!p.active) {
                p.startX = originX + (Random.nextFloat() - 0.5f) * 20f * density
                p.startY = originY + (Random.nextFloat() - 0.5f) * 20f * density
                p.targetX = targetX
                p.targetY = targetY

                // Quadratic control point arched outward
                val midX = (p.startX + targetX) / 2f
                val midY = (p.startY + targetY) / 2f
                val arcOffset = (60f + Random.nextFloat() * 60f) * density * (if (Random.nextBoolean()) 1 else -1)
                p.controlX = midX + arcOffset
                p.controlY = midY

                p.progress = 0f
                p.color = if (lineCount >= 3) Color.parseColor("#FFFFD600") else Color.parseColor("#00E5FF")
                p.size = (4f + Random.nextFloat() * 3f) * density
                p.active = true
                spawned++
            }
        }
    }

    fun updateAndRender(canvas: Canvas, dt: Float) {
        for (i in 0 until poolSize) {
            val p = pool[i]
            if (!p.active) continue

            p.progress += dt * 2.2f // ~0.45s flight time
            if (p.progress >= 1.0f) {
                p.active = false
                continue
            }

            // Quadratic Bezier Evaluation: (1-t)^2 * P0 + 2(1-t)t * P1 + t^2 * P2
            val t = p.progress
            val u = 1.0f - t
            val curX = (u.pow(2) * p.startX) + (2f * u * t * p.controlX) + (t.pow(2) * p.targetX)
            val curY = (u.pow(2) * p.startY) + (2f * u * t * p.controlY) + (t.pow(2) * p.targetY)

            // Outer Glow
            glowPaint.color = p.color
            glowPaint.alpha = (u * 180).toInt().coerceIn(0, 255)
            canvas.drawCircle(curX, curY, p.size * 1.5f, glowPaint)

            // Core Projectile
            paint.color = Color.WHITE
            paint.alpha = (u * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(curX, curY, p.size, paint)
        }
    }
}
