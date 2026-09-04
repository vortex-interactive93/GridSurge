package com.example.gridsurge.game.render

import android.graphics.*
import com.example.gridsurge.R
import kotlin.math.sin

class BlitzClashHudRenderer(private val density: Float) {

    private val barBounds = RectF()
    private val momentumRail = RectF()
    private val pinRect = RectF()
    
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC080D1A")
        style = Paint.Style.FILL
    }
    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF") // Neon Cyan
        style = Paint.Style.FILL
    }
    private val rivalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF0055") // Neon Crimson
        style = Paint.Style.FILL
    }
    private val railPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155")
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = 11f * density
        color = Color.WHITE
    }
    private val timerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = 14f * density
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private var pinBitmap: Bitmap? = null
    private var lastPulseTime = 0L
    private var interpolatedDelta = 0f

    fun prepareDimensions(context: android.content.Context) {
        if (pinBitmap == null) {
            pinBitmap = com.example.gridsurge.game.util.DisplayMetricsPreloader.prewarmedRivalPin
        }
        
        if (pinBitmap == null) {
            try {
                val raw = BitmapFactory.decodeResource(context.resources, R.drawable.ic_rival_ghost)
                if (raw != null) {
                    val targetSize = maxOf(1, (24f * density).toInt())
                    pinBitmap = Bitmap.createScaledBitmap(raw, targetSize, targetSize, true)
                }
            } catch (_: Exception) {}
        }
    }

    fun renderDuelHud(
        canvas: Canvas,
        anchorRect: RectF,
        playerScore: Long,
        rivalScore: Long,
        secondsRemaining: Int,
        rivalComboActive: Boolean,
        now: Long
    ) {
        val hudHeight = 56f * density
        val hudTop = 10f * density
        barBounds.set(anchorRect.left, hudTop, anchorRect.right, hudTop + hudHeight)
        
        // 1. Background Console
        canvas.drawRoundRect(barBounds, 12f * density, 12f * density, bgPaint)

        // 2. Top Row Metrics
        textPaint.textSize = 13f * density
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        // Player Score (Left)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#00E5FF")
        canvas.drawText("YOU: $playerScore", barBounds.left + 16f * density, barBounds.top + 22f * density, textPaint)

        // Rival Score (Right)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.parseColor("#FF0055")
        canvas.drawText("RIVAL: $rivalScore", barBounds.right - 16f * density, barBounds.top + 22f * density, textPaint)

        // 3. Momentum Rail
        val railMargin = 40f * density
        val railY = barBounds.top + 38f * density
        momentumRail.set(barBounds.left + railMargin, railY - 1.5f * density, barBounds.right - railMargin, railY + 1.5f * density)
        canvas.drawRoundRect(momentumRail, 1.5f * density, 1.5f * density, railPaint)

        // 4. Pin Logic
        val scoreDelta = (playerScore - rivalScore).toFloat().coerceIn(-3000f, 3000f)
        interpolatedDelta = interpolatedDelta + (scoreDelta - interpolatedDelta) * 0.1f
        
        val railWidth = momentumRail.width()
        val pinX = momentumRail.centerX() + (interpolatedDelta / 3000f) * (railWidth / 2f)
        
        val pinSize = 22f * density
        pinRect.set(pinX - pinSize / 2f, railY - pinSize / 2f, pinX + pinSize / 2f, railY + pinSize / 2f)

        // 5. Anchored Lead Delta Badge (Pill style)
        textPaint.textSize = 10f * density
        textPaint.textAlign = Paint.Align.CENTER
        val absDelta = Math.abs(playerScore - rivalScore)
        val deltaText = if (playerScore >= rivalScore) "[ +$absDelta LEAD ]" else "[ -$absDelta DEFICIT ]"
        val badgeColor = if (playerScore >= rivalScore) Color.parseColor("#00FF66") else Color.parseColor("#FFD600")
        
        textPaint.color = badgeColor
        canvas.drawText(deltaText, barBounds.centerX(), barBounds.bottom - 6f * density, textPaint)

        // 6. Draw Pin with Pulse
        if (rivalComboActive) lastPulseTime = now
        val pulseElapsed = now - lastPulseTime
        val isPulsing = pulseElapsed < 500
        
        if (isPulsing) {
            val pulse = sin(pulseElapsed / 50.0).toFloat()
            canvas.save()
            canvas.scale(1f + 0.3f * pulse, 1f + 0.3f * pulse, pinRect.centerX(), pinRect.centerY())
        }

        pinBitmap?.let {
            val halfW = it.width / 2f
            val halfH = it.height / 2f
            canvas.drawBitmap(it, pinRect.centerX() - halfW, pinRect.centerY() - halfH, null)
        } ?: run {
            canvas.drawCircle(pinRect.centerX(), pinRect.centerY(), pinSize / 2f, if (scoreDelta >= 0) playerPaint else rivalPaint)
        }

        if (isPulsing) canvas.restore()
    }

    // Deprecated for renderDuelHud
    fun renderDuelBar(canvas: Canvas, anchorRect: RectF, playerScore: Long, rivalScore: Long, rivalComboActive: Boolean, now: Long) {
        renderDuelHud(canvas, anchorRect, playerScore, rivalScore, 0, rivalComboActive, now)
    }

    private val lockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lockReticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = Color.parseColor("#FFFF0055")
    }

    fun renderLockTelegraph(canvas: Canvas, slotRect: RectF, progress: Float, now: Long) {
        val size = slotRect.width() * (1f + (1f - progress))
        val cx = slotRect.centerX()
        val cy = slotRect.centerY()
        
        // Flashing reticle
        if ((now / 100) % 2 == 0L) {
            canvas.drawRect(cx - size/2, cy - size/2, cx + size/2, cy + size/2, lockReticlePaint)
        }
        
        // Corner brackets closing in
        val bracketLen = 12f * density
        canvas.drawLine(slotRect.left, slotRect.top + bracketLen, slotRect.left, slotRect.top, lockReticlePaint)
        canvas.drawLine(slotRect.left, slotRect.top, slotRect.left + bracketLen, slotRect.top, lockReticlePaint)
        
        canvas.drawLine(slotRect.right, slotRect.top + bracketLen, slotRect.right, slotRect.top, lockReticlePaint)
        canvas.drawLine(slotRect.right, slotRect.top, slotRect.right - bracketLen, slotRect.top, lockReticlePaint)
        
        canvas.drawLine(slotRect.left, slotRect.bottom - bracketLen, slotRect.left, slotRect.bottom, lockReticlePaint)
        canvas.drawLine(slotRect.left, slotRect.bottom, slotRect.left + bracketLen, slotRect.bottom, lockReticlePaint)
        
        canvas.drawLine(slotRect.right, slotRect.bottom - bracketLen, slotRect.right, slotRect.bottom, lockReticlePaint)
        canvas.drawLine(slotRect.right, slotRect.bottom, slotRect.right - bracketLen, slotRect.bottom, lockReticlePaint)
    }

    fun renderStasisLock(canvas: Canvas, slotRect: RectF, remainingMs: Long, rattleOffset: Float, now: Long) {
        // 1. Dim the slot
        lockPaint.color = Color.parseColor("#990A0F1D")
        lockPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(slotRect, 12f * density, 12f * density, lockPaint)
        
        // 2. Corner Clamps (Animated snapping)
        lockPaint.color = Color.parseColor("#FF415A77")
        lockPaint.style = Paint.Style.FILL
        val clampSize = 14f * density
        
        canvas.save()
        if (rattleOffset != 0f) {
            canvas.translate(rattleOffset, 0f)
        }
        
        // Top-Left Clamp
        canvas.drawRect(slotRect.left, slotRect.top, slotRect.left + clampSize, slotRect.top + clampSize/2, lockPaint)
        canvas.drawRect(slotRect.left, slotRect.top, slotRect.left + clampSize/2, slotRect.top + clampSize, lockPaint)
        
        // Top-Right
        canvas.drawRect(slotRect.right - clampSize, slotRect.top, slotRect.right, slotRect.top + clampSize/2, lockPaint)
        canvas.drawRect(slotRect.right - clampSize/2, slotRect.top, slotRect.right, slotRect.top + clampSize, lockPaint)
        
        // Bottom-Left
        canvas.drawRect(slotRect.left, slotRect.bottom - clampSize/2, slotRect.left + clampSize, slotRect.bottom, lockPaint)
        canvas.drawRect(slotRect.left, slotRect.bottom - clampSize, slotRect.left + clampSize/2, slotRect.bottom, lockPaint)
        
        // Bottom-Right
        canvas.drawRect(slotRect.right - clampSize, slotRect.bottom - clampSize/2, slotRect.right, slotRect.bottom, lockPaint)
        canvas.drawRect(slotRect.right - clampSize/2, slotRect.bottom - clampSize, slotRect.right, slotRect.bottom, lockPaint)
        
        // 3. Countdown Arc
        val arcPadding = 16f * density
        val arcRect = RectF(slotRect.left + arcPadding, slotRect.top + arcPadding, slotRect.right - arcPadding, slotRect.bottom - arcPadding)
        lockPaint.style = Paint.Style.STROKE
        lockPaint.strokeWidth = 3f * density
        lockPaint.color = Color.parseColor("#3300E5FF")
        canvas.drawOval(arcRect, lockPaint)
        
        lockPaint.color = Color.parseColor("#FF00E5FF")
        val sweep = (remainingMs / 2500f) * 360f
        canvas.drawArc(arcRect, -90f, sweep, false, lockPaint)
        
        // 4. Time Text
        textPaint.textSize = 10f * density
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        val sec = String.format("%.1fs", remainingMs / 1000f)
        canvas.drawText(sec, slotRect.centerX(), slotRect.centerY() + 4f * density, textPaint)
        
        canvas.restore()
    }
}
