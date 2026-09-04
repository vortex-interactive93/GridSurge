package com.example.gridsurge.game.render

import android.graphics.*
import androidx.core.graphics.toColorInt
import com.example.gridsurge.features.adventure.model.BoosterInventoryState
import com.example.gridsurge.features.adventure.model.BoosterType

class BoosterDockRenderer(private val density: Float) {

    val boosterBounds = Array(3) { RectF() }
    private val iconBounds = RectF()
    private val srcRect = Rect()
    
    var rerollRotation = 0f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = "#E6080D1A".toColorInt()
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    private val activeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = "#4D00E5FF".toColorInt()
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = 9f * density
        color = Color.WHITE
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        textSize = 8.5f * density
        color = Color.BLACK
    }
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#00E5FF")
    }

    fun layout(dockBounds: RectF) {
        val btnHeight = 36f * density
        val btnTop = dockBounds.bottom + (10f * density)
        val spacing = 8f * density
        val btnWidth = (dockBounds.width() - (spacing * 2)) / 3f

        for (i in 0 until 3) {
            val left = dockBounds.left + i * (btnWidth + spacing)
            boosterBounds[i].set(left, btnTop, left + btnWidth, btnTop + btnHeight)
        }
    }

    fun renderBoosters(canvas: Canvas, inventory: BoosterInventoryState, now: Long, textureCache: BlockTextureCache) {
        val boosters = listOf(
            Triple(BoosterType.EMP_HAMMER, inventory.empHammerCount, textureCache.icEmpStrike),
            Triple(BoosterType.QUANTUM_REROLL, inventory.quantumRerollCount, textureCache.icReroll),
            Triple(BoosterType.CATALYST_CONVERTER, inventory.catalystConverterCount, textureCache.icOverdrive)
        )

        for (i in 0 until 3) {
            val rect = boosterBounds[i]
            val (type, count, icon) = boosters[i]
            val isActive = inventory.activeTargetingBooster == type
            val hasCharges = count > 0

            // Card Fill & Hardware Bevel
            bgPaint.alpha = if (hasCharges) 255 else (255 * 0.6f).toInt()
            canvas.drawRoundRect(rect, 8f * density, 8f * density, bgPaint)

            if (isActive) {
                val pulse = (kotlin.math.sin(now / 90.0) * 0.5 + 0.5).toFloat()
                activeGlowPaint.alpha = (100 + pulse * 120).toInt()
                canvas.drawRoundRect(rect, 8f * density, 8f * density, activeGlowPaint)
                borderPaint.color = Color.parseColor("#00E5FF")
            } else {
                borderPaint.color = if (hasCharges) Color.parseColor("#1C2C4A") else Color.parseColor("#0E1624")
            }

            canvas.drawRoundRect(rect, 8f * density, 8f * density, borderPaint)

            // Icon Rendering
            icon?.let { bmp ->
                val iconSize = 22f * density
                iconBounds.set(
                    rect.centerX() - iconSize / 2f,
                    rect.centerY() - iconSize / 2f,
                    rect.centerX() + iconSize / 2f,
                    rect.centerY() + iconSize / 2f
                )
                
                srcRect.set(0, 0, bmp.width, bmp.height)
                
                canvas.save()
                if (type == BoosterType.QUANTUM_REROLL) {
                    canvas.rotate(rerollRotation, rect.centerX(), rect.centerY())
                }
                
                // Dimming when 0 charges
                val iconPaint = textureCache.filterPaint
                iconPaint.alpha = if (hasCharges) 255 else (255 * 0.4f).toInt()
                canvas.drawBitmap(bmp, srcRect, iconBounds, iconPaint)
                iconPaint.alpha = 255
                canvas.restore()
            }

            // Count Badge
            val badgeRadius = 6.5f * density
            val bx = rect.right - badgeRadius - (3f * density)
            val by = rect.top + badgeRadius + (3f * density)
            badgeBgPaint.color = if (hasCharges) Color.parseColor("#00E5FF") else Color.parseColor("#263859")
            canvas.drawCircle(bx, by, badgeRadius, badgeBgPaint)
            
            badgePaint.color = if (hasCharges) Color.BLACK else Color.parseColor("#55657E")
            canvas.drawText("$count", bx, by + (3f * density), badgePaint)
        }
    }
}
