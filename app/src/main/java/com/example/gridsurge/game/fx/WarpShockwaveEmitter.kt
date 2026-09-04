package com.example.gridsurge.game.fx

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.animation.DecelerateInterpolator

class WarpShockwaveEmitter(private val density: Float) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var isActive = false
    private var startTime = 0L
    private val duration = 320L
    private val interpolator = DecelerateInterpolator(2.0f)

    private var centerX = 0f
    private var centerY = 0f
    private var maxRadius = 0f

    private val sparkCount = 24
    private val sparks = Array(sparkCount) { Spark() }

    private class Spark {
        var x = 0f
        var y = 0f
        var vx = 0f
        var vy = 0f
        var radius = 0f
        var color = Color.WHITE
    }

    fun trigger(cx: Float, cy: Float, radius: Float) {
        centerX = cx
        centerY = cy
        maxRadius = radius * 2.5f
        startTime = System.currentTimeMillis()
        isActive = true

        // Initialize sparks
        for (i in 0 until sparkCount) {
            val angle = (Math.random() * Math.PI * 2).toFloat()
            val speed = (4f + Math.random().toFloat() * 6f) * density
            sparks[i].apply {
                x = cx
                y = cy
                vx = Math.cos(angle.toDouble()).toFloat() * speed
                vy = Math.sin(angle.toDouble()).toFloat() * speed
                this.radius = (2f + Math.random().toFloat() * 2f) * density
                color = if (Math.random() > 0.5) Color.parseColor("#00E5FF") else Color.WHITE
            }
        }
    }

    fun draw(canvas: Canvas, now: Long): Boolean {
        if (!isActive) return false

        val elapsed = now - startTime
        val rawProgress = elapsed.toFloat() / duration
        if (rawProgress >= 1f) {
            isActive = false
            return false
        }

        val t = interpolator.getInterpolation(rawProgress)
        val alpha = (255 * (1f - t)).toInt()

        // 1. Shockwave Rings
        ringPaint.strokeWidth = (4f * (1f - t) + 1f) * density
        
        // Cyan Ring
        ringPaint.color = Color.parseColor("#00E5FF")
        ringPaint.alpha = alpha
        canvas.drawCircle(centerX, centerY, maxRadius * t, ringPaint)

        // Magenta Ring (slight delay or different scale)
        val t2 = interpolator.getInterpolation((rawProgress * 0.9f).coerceIn(0f, 1f))
        ringPaint.color = Color.parseColor("#FF007F")
        ringPaint.alpha = (alpha * 0.8f).toInt()
        canvas.drawCircle(centerX, centerY, maxRadius * t2 * 0.8f, ringPaint)

        // 2. Radial Sparks
        for (spark in sparks) {
            spark.x += spark.vx
            spark.y += spark.vy
            spark.vx *= 0.92f // Drag
            spark.vy *= 0.92f
            
            val currentRadius = spark.radius * (1f - t)
            sparkPaint.color = spark.color
            sparkPaint.alpha = alpha
            canvas.drawCircle(spark.x, spark.y, currentRadius, sparkPaint)
        }

        return true
    }
}
