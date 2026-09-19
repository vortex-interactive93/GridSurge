package com.example.gridsurge.game.render

import android.graphics.*
import kotlin.math.sin

class CriticalVignetteRenderer(private val density: Float) {

    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var cachedWidth = 0f
    private var cachedHeight = 0f
    private var vignetteShader: RadialGradient? = null

    fun updateDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f || (width == cachedWidth && height == cachedHeight)) return
        cachedWidth = width
        cachedHeight = height

        val radius = maxOf(width, height) * 0.75f
        vignetteShader = RadialGradient(
            width / 2f, height / 2f, radius,
            intArrayOf(Color.TRANSPARENT, Color.parseColor("#33FF0055"), Color.parseColor("#99FF0022")),
            floatArrayOf(0.0f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
        vignettePaint.shader = vignetteShader
    }

    fun renderVignette(canvas: Canvas, secondsRemaining: Float, now: Long) {
        if (secondsRemaining > 15.0f || cachedWidth <= 0f) return

        // Escalating frequency as time runs out
        val urgency = (1.0f - (secondsRemaining / 15.0f)).coerceIn(0f, 1f)
        val freq = 80.0 + (1.0 - urgency) * 120.0
        val pulse = (sin(now / freq) * 0.5 + 0.5).toFloat()

        vignettePaint.alpha = (50 + pulse * 140 * urgency).toInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, cachedWidth, cachedHeight, vignettePaint)
    }
}
