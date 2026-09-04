package com.example.gridsurge.features.adventure.rendering

import android.content.Context
import android.graphics.*
import com.example.gridsurge.R
import com.example.gridsurge.features.adventure.model.SectorCoreType
import kotlin.math.sin

class SectorCoreTextureRenderer(
    private val context: Context,
    private val density: Float
) {
    private val prewarmedBitmaps = mutableMapOf<SectorCoreType, Bitmap>()
    private val prewarmedCrackedBitmaps = mutableMapOf<SectorCoreType, Bitmap>()
    private var unlockedCipherBitmap: Bitmap? = null

    // Pre-allocated Drawing Objects
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    
    // Dedicated Ambient (Blur) Paint Pass
    private val ambientGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.5f * density
        maskFilter = BlurMaskFilter(4f * density, BlurMaskFilter.Blur.NORMAL)
    }

    // Dedicated Crisp Border Paint Pass
    private val crispBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2.0f * density
    }

    // Badge Render Objects
    private val badgeBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC1A1A1A")
        style = Paint.Style.FILL
    }
    
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    
    private val badgeBoundsRect = RectF()
    private val cachedFontMetrics = Paint.FontMetrics()

    // Crack Vector Fallback
    private val crackGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 3.5f * density
        maskFilter = BlurMaskFilter(3f * density, BlurMaskFilter.Blur.NORMAL)
    }

    private val crackCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFE0")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 1.5f * density
    }

    private val sparkDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val crackPath = Path()
    private val tempCellRect = RectF()

    // Lookup Table for Numeric Strings 0..99 (Zero-Allocation Int Formatting)
    private val digitStringLut = Array(100) { it.toString() }

    fun prepareBitmaps(cellSizePx: Int) {
        if (cellSizePx <= 0) return

        SectorCoreType.entries.forEach { coreType ->
            try {
                val srcBitmap = BitmapFactory.decodeResource(context.resources, coreType.defaultDrawableRes)
                if (srcBitmap != null) {
                    prewarmedBitmaps[coreType] = Bitmap.createScaledBitmap(srcBitmap, cellSizePx, cellSizePx, true)
                }

                if (coreType.crackedDrawableRes != null) {
                    val crackedSrc = BitmapFactory.decodeResource(context.resources, coreType.crackedDrawableRes)
                    if (crackedSrc != null) {
                        prewarmedCrackedBitmaps[coreType] = Bitmap.createScaledBitmap(crackedSrc, cellSizePx, cellSizePx, true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SectorCoreRenderer", "Bitmap prewarm failed for ${coreType.name}: ${e.message}")
            }
        }

        try {
            val cipherUnlockedSrc = BitmapFactory.decodeResource(context.resources, R.drawable.sector_3_block_no_lock)
            if (cipherUnlockedSrc != null) {
                unlockedCipherBitmap = Bitmap.createScaledBitmap(cipherUnlockedSrc, cellSizePx, cellSizePx, true)
            }
        } catch (_: Exception) {}
    }

    fun drawSectorCore(
        canvas: Canvas,
        rect: RectF,
        coreType: SectorCoreType,
        isCracked: Boolean,
        isUnlocked: Boolean,
        isMeltdown: Boolean,
        now: Long,
        maxAllowedWidth: Float = 0f,
        turnsRemaining: Int = 99
    ) {
        if (rect.width() <= 0f || (maxAllowedWidth > 0f && rect.width() > maxAllowedWidth * 1.5f)) {
            return
        }

        val targetBitmap: Bitmap = (if (isCracked) {
            prewarmedCrackedBitmaps[coreType] ?: prewarmedBitmaps[coreType]
        } else {
            if (coreType == SectorCoreType.CRIMSON_CIPHER_SEC3 && isUnlocked) {
                unlockedCipherBitmap ?: prewarmedBitmaps[coreType]
            } else {
                prewarmedBitmaps[coreType]
            }
        }) ?: return

        val isCritical = turnsRemaining == 1 || isMeltdown
        val pulseFreq = if (isCracked || isCritical) 90.0 else 160.0
        val pulseAmp = if (isCracked || isCritical) 0.06f else 0.04f
        val pulse = (sin(now / pulseFreq) * 0.5 + 0.5).toFloat()
        val scale = 1.0f + (pulse * pulseAmp)

        var offsetX = 0f
        var offsetY = 0f
        if (isCritical) {
            val freq = if (isMeltdown) 0.085 else 0.045
            val shakeAmp = if (isMeltdown) 0.08f else 0.04f
            offsetX = (sin(now * freq) * rect.width() * shakeAmp).toFloat()
            offsetY = (sin(now * freq * 1.2) * rect.width() * shakeAmp).toFloat()
        }

        val cx = rect.centerX()
        val cy = rect.centerY()

        val saveCount = canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale, cx, cy)
        canvas.drawBitmap(targetBitmap, null, rect, drawPaint)

        // Emissive Color Lookup
        val glowColor = when (coreType) {
            SectorCoreType.CHRONO_REACTOR_SEC1 -> if (isCracked) 0xFFFF1744.toInt() else 0xFF00E5FF.toInt()
            SectorCoreType.SOLAR_CRUCIBLE_SEC2 -> if (isMeltdown) 0xFFFF1744.toInt() else if (turnsRemaining == 1) 0xFFFF9900.toInt() else 0xFFFFD600.toInt()
            SectorCoreType.CRIMSON_CIPHER_SEC3 -> 0xFFFF1744.toInt()
            SectorCoreType.BIO_CONDUIT_SEC4 -> 0xFF00FF66.toInt()
            SectorCoreType.VOID_SINGULARITY_SEC5 -> 0xFF8A2BE2.toInt()
        }

        val baseAlpha = if (isCracked || isCritical) 0.75f else 0.65f
        val alphaMultiplier = baseAlpha + (1f - baseAlpha) * pulse

        // Pass 1: Soft Ambient Halo
        ambientGlowPaint.color = glowColor
        ambientGlowPaint.alpha = (alphaMultiplier * 0.35f * 255).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, ambientGlowPaint)

        // Pass 2: Hard Core Perimeter
        crispBorderPaint.color = glowColor
        crispBorderPaint.alpha = (alphaMultiplier * 255).toInt()
        canvas.drawRoundRect(rect, 8f * density, 8f * density, crispBorderPaint)

        // Pass 3: Vulnerability Flare Outline (P2 Fix)
        if (isUnlocked && coreType == SectorCoreType.CRIMSON_CIPHER_SEC3) {
            val flarePulse = (sin(now / 70.0) * 0.5 + 0.5).toFloat()
            crispBorderPaint.color = Color.parseColor("#FF00FF") // Neon Magenta
            crispBorderPaint.strokeWidth = 3.5f * density
            crispBorderPaint.alpha = (160 + 95 * flarePulse).toInt()
            
            val outset = 2f * density
            tempCellRect.set(rect.left - outset, rect.top - outset, rect.right + outset, rect.bottom + outset)
            canvas.drawRoundRect(tempCellRect, 10f * density, 10f * density, crispBorderPaint)
            crispBorderPaint.strokeWidth = 2.0f * density // Restore
        }

        // Countdown HUD Badge
        if (coreType == SectorCoreType.SOLAR_CRUCIBLE_SEC2 && (turnsRemaining < 99 || isMeltdown)) {
            val bSize = rect.width() * 0.42f
            val margin = rect.width() * 0.06f
            badgeBoundsRect.set(
                rect.right - bSize - margin,
                rect.top + margin,
                rect.right - margin,
                rect.top + bSize + margin
            )
            
            val badgeColor = if (isMeltdown) Color.parseColor("#FF0055") else Color.parseColor("#CC1A1A1A")
            badgeBackgroundPaint.color = badgeColor
            canvas.drawRoundRect(badgeBoundsRect, bSize * 0.3f, bSize * 0.3f, badgeBackgroundPaint)
            
            val text = if (isMeltdown) "0!" else digitStringLut[turnsRemaining.coerceIn(0, 99)]
            badgeTextPaint.textSize = if (isMeltdown || turnsRemaining >= 10) bSize * 0.58f else bSize * 0.7f
            
            // Zero-GC Baseline Vertical Alignment via cached metrics buffer
            badgeTextPaint.getFontMetrics(cachedFontMetrics)
            val baselineOffset = (cachedFontMetrics.descent - cachedFontMetrics.ascent) / 2f - cachedFontMetrics.descent
            val textY = badgeBoundsRect.centerY() + baselineOffset

            canvas.drawText(text, badgeBoundsRect.centerX(), textY, badgeTextPaint)
        }

        // Procedural Crack Fallback
        if (isCracked && targetBitmap == prewarmedBitmaps[coreType]) {
            drawFractureLines(canvas, rect, coreType, now)
        }

        canvas.restoreToCount(saveCount)
    }

    private fun drawFractureLines(canvas: Canvas, rect: RectF, coreType: SectorCoreType, now: Long) {
        val crackGlowColor = when (coreType) {
            SectorCoreType.SOLAR_CRUCIBLE_SEC2 -> 0xFFFF3D00.toInt() // Molten Orange/Red
            SectorCoreType.CHRONO_REACTOR_SEC1 -> 0xFFFF1744.toInt() // Deep Red Breach
            SectorCoreType.BIO_CONDUIT_SEC4 -> 0xFF00FF66.toInt() // Toxic Slime Green
            else -> 0xFFFF0055.toInt() // Cyber Neon Pink
        }

        val pulse = (sin(now / 110.0) * 0.5 + 0.5).toFloat()
        val l = rect.left
        val t = rect.top
        val r = rect.right
        val b = rect.bottom
        val w = rect.width()
        val h = rect.height()

        // 1. Overheating Breached Inner Border Glow
        crispBorderPaint.color = crackGlowColor
        crispBorderPaint.strokeWidth = 2.5f * density
        crispBorderPaint.alpha = (180 + 75 * pulse).toInt()
        val insetRect = tempCellRect
        insetRect.set(l + 2f * density, t + 2f * density, r - 2f * density, b - 2f * density)
        canvas.drawRoundRect(insetRect, 6f * density, 6f * density, crispBorderPaint)
        crispBorderPaint.strokeWidth = 2.0f * density // Restore

        // 2. Jagged Electric Lightning Fracture Paths
        crackPath.rewind()

        // Main Diagonal Fracture (Jagged branching path 1)
        crackPath.moveTo(l + w * 0.15f, t + h * 0.18f)
        crackPath.lineTo(l + w * 0.32f, t + h * 0.28f)
        crackPath.lineTo(l + w * 0.28f, t + h * 0.45f)
        crackPath.lineTo(l + w * 0.52f, t + h * 0.54f) // Main junction 1
        crackPath.lineTo(l + w * 0.68f, t + h * 0.72f)
        crackPath.lineTo(l + w * 0.82f, b - h * 0.12f)

        // Branch 1 (Upper right)
        crackPath.moveTo(l + w * 0.52f, t + h * 0.54f)
        crackPath.lineTo(l + w * 0.75f, t + h * 0.38f)
        crackPath.lineTo(r - w * 0.12f, t + h * 0.22f)

        // Branch 2 (Lower left)
        crackPath.moveTo(l + w * 0.28f, t + h * 0.45f)
        crackPath.lineTo(l + w * 0.14f, t + h * 0.68f)

        // Pass 1: Outer Molten Glow
        crackGlowPaint.color = crackGlowColor
        crackGlowPaint.alpha = (200 + 55 * pulse).toInt()
        canvas.drawPath(crackPath, crackGlowPaint)

        // Pass 2: Hot Inner Spark Line
        canvas.drawPath(crackPath, crackCorePaint)

        // 3. Hot Spark Dots at Junctions
        canvas.drawCircle(l + w * 0.52f, t + h * 0.54f, 2f * density, sparkDotPaint)
        canvas.drawCircle(l + w * 0.28f, t + h * 0.45f, 1.5f * density, sparkDotPaint)
    }
}
