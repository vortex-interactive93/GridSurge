package com.example.gridsurge.game.glitch.render

import android.content.Context
import android.graphics.*
import android.util.Log
import com.example.gridsurge.R
import com.example.gridsurge.game.glitch.model.AnomalyStage
import com.example.gridsurge.game.glitch.model.StagedGlitchBlock
import kotlin.math.sin

class StagedAnomalyBitmapRenderer(
    private val context: Context,
    private val density: Float
) {
    private val prewarmedBitmaps = mutableMapOf<AnomalyStage, Bitmap>()
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    // Ambient Glow Paint Pass
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f * density
        maskFilter = BlurMaskFilter(4f * density, BlurMaskFilter.Blur.NORMAL)
    }

    // Corner Badge Paints
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val badgeRect = RectF()
    private val fontMetrics = Paint.FontMetrics()

    /**
     * Call once inside onSizeChanged() to pre-scale bitmaps to physical cell dimensions.
     */
    fun prewarmBitmaps(cellSizePx: Int) {
        if (cellSizePx <= 0) return

        val stageDrawables = mapOf(
            AnomalyStage.STAGE_1_CONTAINED to R.drawable.block_glitch_s1_contained,
            AnomalyStage.STAGE_2_UNSTABLE to R.drawable.block_glitch_s2_unstable,
            AnomalyStage.STAGE_3_CRITICAL to R.drawable.block_glitch_s3_critical,
            AnomalyStage.STAGE_4_OBSIDIAN_SLAG to R.drawable.block_glitch_s4_slag
        )

        stageDrawables.forEach { (stage, resId) ->
            try {
                val rawBitmap = BitmapFactory.decodeResource(context.resources, resId)
                if (rawBitmap != null) {
                    prewarmedBitmaps[stage] = Bitmap.createScaledBitmap(rawBitmap, cellSizePx, cellSizePx, true)
                }
            } catch (e: Exception) {
                Log.e("AnomalyRenderer", "Failed prewarming bitmap for $stage", e)
            }
        }
    }

    fun renderAnomaly(
        canvas: Canvas,
        rect: RectF,
        anomaly: StagedGlitchBlock,
        now: Long
    ) {
        val stage = anomaly.currentStage
        val bitmap = prewarmedBitmaps[stage] ?: return

        // 1. Kinetic Breathing Pulse
        val pulseFreq = when (stage) {
            AnomalyStage.STAGE_3_CRITICAL -> 50.0 // Urgent fast pulse
            AnomalyStage.STAGE_2_UNSTABLE -> 80.0
            AnomalyStage.STAGE_1_CONTAINED -> 140.0
            AnomalyStage.STAGE_4_OBSIDIAN_SLAG -> 240.0 // Slow dead fissure pulse
        }
        val pulse = (sin(now / pulseFreq) * 0.5 + 0.5).toFloat()
        val scale = if (stage == AnomalyStage.STAGE_3_CRITICAL) 1.0f + (pulse * 0.04f) else 1.0f

        val cx = rect.centerX()
        val cy = rect.centerY()

        val saveCount = canvas.save()
        if (scale != 1.0f) {
            canvas.scale(scale, scale, cx, cy)
        }

        // 2. Render Emissive Ambient Back-Glow
        if (!stage.isSlag) {
            glowPaint.color = when (stage) {
                AnomalyStage.STAGE_1_CONTAINED -> 0xFF00FF66.toInt()
                AnomalyStage.STAGE_2_UNSTABLE -> 0xFFFFD600.toInt()
                AnomalyStage.STAGE_3_CRITICAL -> 0xFFFF0055.toInt()
                else -> Color.TRANSPARENT
            }
            glowPaint.alpha = (80 + pulse * 100).toInt().coerceIn(0, 255)
            canvas.drawRoundRect(rect, 8f * density, 8f * density, glowPaint)
        }

        // 3. Draw Pre-scaled Texture Block
        canvas.drawBitmap(bitmap, null, rect, drawPaint)

        // 4. Tactical Top-Right Corner Turn Badge (Stages 1-3)
        if (!stage.isSlag && stage.turnsRemaining > 0) {
            val bSize = rect.width() * 0.38f
            val margin = rect.width() * 0.05f

            badgeRect.set(
                rect.right - bSize - margin,
                rect.top + margin,
                rect.right - margin,
                rect.top + bSize + margin
            )

            badgeBgPaint.color = when (stage) {
                AnomalyStage.STAGE_3_CRITICAL -> 0xCCFF0055.toInt()
                AnomalyStage.STAGE_2_UNSTABLE -> 0xCCFFD600.toInt()
                else -> 0xCC003816.toInt()
            }
            canvas.drawRoundRect(badgeRect, 4f * density, 4f * density, badgeBgPaint)

            // Center Turn Counter Baseline
            badgeTextPaint.textSize = bSize * 0.65f
            badgeTextPaint.getFontMetrics(fontMetrics)
            val baseline = badgeRect.centerY() + (fontMetrics.descent - fontMetrics.ascent) / 2f - fontMetrics.descent
            canvas.drawText("${stage.turnsRemaining}", badgeRect.centerX(), baseline, badgeTextPaint)
        }

        canvas.restoreToCount(saveCount)
    }
}
