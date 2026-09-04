package com.example.gridsurge.game.fx

import android.graphics.*
import com.example.gridsurge.game.juice.DangerLevel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 down to 0.0
    val color: Int,
    val size: Float
)


data class LandingPulseEntity(
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = 0f,
    var maxRadius: Float = 0f,
    var life: Float = 0f,
    var color: Int = Color.CYAN,
    var isActive: Boolean = false
)

data class CorruptionPulseEntity(
    var startX: Float = 0f,
    var startY: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var progress: Float = 0f,
    var isActive: Boolean = false
)

class JuiceFxEngine(private val density: Float) {

    private val particles = mutableListOf<Particle>()

    // 1-Frame Laser Impact Flash Matrix (Index 0..63 -> Alpha 0..255)
    private val cellFlashAlphas = IntArray(64) { 0 }

    private val landingPulses = Array(12) { LandingPulseEntity() }
    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }

    private val pulsePool = Array(6) { CorruptionPulseEntity() }
    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#00FF66") // Neon Glitch Emerald
    }

    // Pre-allocated Paints
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val criticalVignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f * density
        color = Color.parseColor("#FF0055")
    }
    private val warningVignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        color = Color.parseColor("#FFD600")
    }
    private val cornerBracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        strokeCap = Paint.Cap.SQUARE
    }
    private val cornerBracketPath = Path()

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var glitchIntensity: Float = 0f
    private var glitchDuration: Float = 0.22f // ~220ms quick burst
    private var glitchTimeRemaining: Float = 0f

    // Reusable paints for zero-allocation rendering per frame
    private val redSlicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80FF0055") // Neon Crimson with 50% alpha
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    private val cyanSlicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8000E5FF") // Neon Cyan with 50% alpha
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    private val sliceRect = RectF()

    /**
     * Call this when a Catalyst node is detonated.
     */
    fun triggerCatalystGlitchFlash(intensity: Float = 1.0f) {
        this.glitchIntensity = intensity
        this.glitchTimeRemaining = glitchDuration
    }

    fun clearAll() {
        particles.clear()
        cellFlashAlphas.fill(0)
        glitchTimeRemaining = 0f
        pulsePool.forEach { it.isActive = false }
        landingPulses.forEach { it.isActive = false }
    }

    fun isGlitchActive(): Boolean = glitchTimeRemaining > 0f

    /**
     * Spawns an expanding neon shockwave pulse when a piece lands.
     */
    fun spawnLandingShockwave(x: Float, y: Float, color: Int) {
        val pulse = landingPulses.firstOrNull { !it.isActive } ?: landingPulses[0]
        pulse.apply {
            this.x = x
            this.y = y
            this.radius = 4f * density
            this.maxRadius = 26f * density
            this.life = 1f
            this.color = color
            this.isActive = true
        }
        spawnBurstParticles(x, y, color, count = 3)
    }

    fun triggerLineImpactFlash(rowsMask: Int, colsMask: Int) {
        for (r in 0 until 8) {
            if ((rowsMask and (1 shl r)) != 0) {
                for (c in 0 until 8) cellFlashAlphas[r * 8 + c] = 255
            }
        }
        for (c in 0 until 8) {
            if ((colsMask and (1 shl c)) != 0) {
                for (r in 0 until 8) cellFlashAlphas[r * 8 + c] = 255
            }
        }
    }

    fun spawnBurstParticles(x: Float, y: Float, color: Int, count: Int = 12) {
        repeat(count) {
            val angle = Random.nextFloat() * Math.PI * 2
            val speed = (1.5f + Random.nextFloat() * 3.5f) * density
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle).toFloat() * speed,
                    vy = sin(angle).toFloat() * speed,
                    life = 1f,
                    color = color,
                    size = (1.5f + Random.nextFloat() * 2.5f) * density
                )
            )
        }
    }


    fun spawnCorruptionSpread(startX: Float, startY: Float, targetX: Float, targetY: Float) {
        val entity = pulsePool.firstOrNull { !it.isActive } ?: pulsePool[0]
        entity.apply {
            this.startX = startX
            this.startY = startY
            this.targetX = targetX
            this.targetY = targetY
            this.progress = 0f
            this.isActive = true
        }
    }

    fun updateFrame(dtSec: Float): Boolean {
        var active = false

        // Update particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx
            p.y += p.vy
            p.life -= dtSec * 1.8f
            if (p.life <= 0f) pIter.remove() else active = true
        }

        val decayAmount = (dtSec * 3000f).toInt().coerceAtLeast(1)
        for (i in 0..63) {
            if (cellFlashAlphas[i] > 0) {
                cellFlashAlphas[i] = (cellFlashAlphas[i] - decayAmount).coerceAtLeast(0)
                if (cellFlashAlphas[i] > 0) active = true
            }
        }
        
        // Update landing pulses
        landingPulses.filter { it.isActive }.forEach { pulse ->
            pulse.radius += (pulse.maxRadius - pulse.radius) * dtSec * 14f
            pulse.life -= dtSec * 3.8f
            if (pulse.life <= 0f) {
                pulse.isActive = false
            } else {
                active = true
            }
        }

        // Update corruption pulses
        pulsePool.filter { it.isActive }.forEach { pulse ->
            pulse.progress += dtSec * 3.5f
            if (pulse.progress >= 1f) {
                pulse.isActive = false
            } else {
                active = true
            }
        }
        
        if (glitchTimeRemaining > 0f) {
            glitchTimeRemaining -= dtSec
            if (glitchTimeRemaining > 0f) active = true
        }

        return active
    }

    fun renderParticles(canvas: Canvas) {
        // Render Landing Shockwaves
        landingPulses.filter { it.isActive }.forEach { pulse ->
            shockwavePaint.color = pulse.color
            shockwavePaint.alpha = (pulse.life.coerceIn(0f, 1f) * 220).toInt()
            shockwavePaint.strokeWidth = (3.5f * pulse.life).coerceAtLeast(1f) * density
            canvas.drawCircle(pulse.x, pulse.y, pulse.radius, shockwavePaint)
        }

        for (p in particles) {
            particlePaint.color = p.color
            particlePaint.alpha = (p.life * 255).toInt()
            canvas.drawCircle(p.x, p.y, p.size * p.life, particlePaint)
        }
    }


    fun renderCorruptionPulses(canvas: Canvas) {
        pulsePool.filter { it.isActive }.forEach { pulse ->
            val curX = pulse.startX + (pulse.targetX - pulse.startX) * pulse.progress
            val curY = pulse.startY + (pulse.targetY - pulse.startY) * pulse.progress
            
            pulsePaint.alpha = ((1f - pulse.progress) * 255).toInt()
            pulsePaint.strokeWidth = (6f - pulse.progress * 4f) * density
            
            // Draw tendril connecting the cells
            canvas.drawLine(pulse.startX, pulse.startY, curX, curY, pulsePaint)
            // Expanding impact ring at target cell
            canvas.drawCircle(curX, curY, pulse.progress * 24f * density, pulsePaint)
        }
    }

    /**
     * Updates and renders chromatic horizontal slice tears and digital glitch bars.
     */
    fun renderGlitchOverlay(canvas: Canvas, width: Float, height: Float) {
        if (glitchTimeRemaining <= 0f) return

        val progress = (glitchTimeRemaining / glitchDuration).coerceIn(0f, 1f)
        val currentAlpha = (progress * 255 * glitchIntensity).toInt()

        // 1. Initial full-screen micro-flash
        if (progress > 0.6f) {
            flashPaint.alpha = ((progress - 0.6f) / 0.4f * 80 * glitchIntensity).toInt()
            canvas.drawRect(0f, 0f, width, height, flashPaint)
        }

        // 2. Horizontal Digital Slices / Chromatic Tear Bars
        redSlicePaint.alpha = currentAlpha
        cyanSlicePaint.alpha = currentAlpha

        val sliceCount = 5
        val maxOffset = 18f * density * progress

        repeat(sliceCount) {
            val sliceH = Random.nextFloat() * (22f * density) + (8f * density)
            val sliceY = Random.nextFloat() * (height - sliceH)
            val shiftX = (Random.nextFloat() * 2f - 1f) * maxOffset

            // Red channel shift (Left)
            sliceRect.set(0f + shiftX, sliceY, width + shiftX, sliceY + sliceH)
            canvas.drawRect(sliceRect, redSlicePaint)

            // Cyan channel shift (Right)
            sliceRect.set(0f - shiftX, sliceY + (2f * density), width - shiftX, sliceY + sliceH + (2f * density))
            canvas.drawRect(sliceRect, cyanSlicePaint)
        }

        // 3. High-frequency digital scanline flicker
        if (Random.nextBoolean()) {
            val lineY = Random.nextFloat() * height
            sliceRect.set(0f, lineY, width, lineY + (1.5f * density))
            canvas.drawRect(sliceRect, redSlicePaint)
        }
    }

    fun renderCellFlash(canvas: Canvas, rect: RectF, index: Int) {
        val alpha = cellFlashAlphas[index]
        if (alpha > 0) {
            flashPaint.alpha = alpha
            canvas.drawRoundRect(rect, 6f * density, 6f * density, flashPaint)
        }
    }

    fun renderDangerTension(canvas: Canvas, gridBounds: RectF, dangerLevel: DangerLevel, now: Long) {
        when (dangerLevel) {
            DangerLevel.CRITICAL -> {
                val pulse = (sin(now / 55.0) * 0.5 + 0.5).toFloat()
                criticalVignettePaint.alpha = (120 + (pulse * 135)).toInt()
                canvas.drawRoundRect(gridBounds, 16f * density, 16f * density, criticalVignettePaint)
                renderCornerReticles(canvas, gridBounds, Color.parseColor("#FF0055"), pulse)
            }
            DangerLevel.WARNING -> {
                val pulse = (sin(now / 140.0) * 0.5 + 0.5).toFloat()
                warningVignettePaint.alpha = (75 + (pulse * 95)).toInt()
                canvas.drawRoundRect(gridBounds, 16f * density, 16f * density, warningVignettePaint)
                renderCornerReticles(canvas, gridBounds, Color.parseColor("#FFD600"), pulse)
            }
            DangerLevel.SAFE -> {}
        }
    }

    private fun renderCornerReticles(canvas: Canvas, bounds: RectF, color: Int, pulse: Float) {
        cornerBracketPaint.color = color
        cornerBracketPaint.alpha = (160 + (pulse * 95)).toInt()
        val len = 14f * density
        val pad = 4f * density

        cornerBracketPath.reset()
        cornerBracketPath.moveTo(bounds.left - pad, bounds.top - pad + len)
        cornerBracketPath.lineTo(bounds.left - pad, bounds.top - pad)
        cornerBracketPath.lineTo(bounds.left - pad + len, bounds.top - pad)
        canvas.drawPath(cornerBracketPath, cornerBracketPaint)

        cornerBracketPath.reset()
        cornerBracketPath.moveTo(bounds.right + pad - len, bounds.top - pad)
        cornerBracketPath.lineTo(bounds.right + pad, bounds.top - pad)
        cornerBracketPath.lineTo(bounds.right + pad, bounds.top - pad + len)
        canvas.drawPath(cornerBracketPath, cornerBracketPaint)

        cornerBracketPath.reset()
        cornerBracketPath.moveTo(bounds.right + pad, bounds.bottom + pad - len)
        cornerBracketPath.lineTo(bounds.right + pad, bounds.bottom + pad)
        cornerBracketPath.lineTo(bounds.right + pad - len, bounds.bottom + pad)
        canvas.drawPath(cornerBracketPath, cornerBracketPaint)

        cornerBracketPath.reset()
        cornerBracketPath.moveTo(bounds.left - pad + len, bounds.bottom + pad)
        cornerBracketPath.lineTo(bounds.left - pad, bounds.bottom + pad)
        cornerBracketPath.lineTo(bounds.left - pad, bounds.bottom + pad - len)
        canvas.drawPath(cornerBracketPath, cornerBracketPaint)
    }
}
