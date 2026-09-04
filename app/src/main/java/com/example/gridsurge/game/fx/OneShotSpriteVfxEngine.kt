package com.example.gridsurge.game.fx

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import com.example.gridsurge.R

enum class SpriteVfxType(
    val drawableRes: Int,
    val cols: Int = 4,
    val rows: Int = 4,
    val totalFrames: Int = 16,
    val durationMs: Long = 320L
) {
    ROW_LASER(R.drawable.vfx_laser_sweep, cols = 4, rows = 4, totalFrames = 16, durationMs = 300L),
    ROW_LIGHTNING(R.drawable.vfx_lightning_horizontal, cols = 4, rows = 4, totalFrames = 16, durationMs = 320L),
    COL_LIGHTNING(R.drawable.vfx_lightning_vertical, cols = 4, rows = 4, totalFrames = 16, durationMs = 320L),
    CROSS_BURST(R.drawable.vfx_cross_laser, cols = 4, rows = 4, totalFrames = 16, durationMs = 350L),
    CORE_SHATTER_CYBER(R.drawable.vfx_crystal_shards, cols = 4, rows = 4, totalFrames = 16, durationMs = 380L),
    CORE_SHATTER_TOXIC(R.drawable.vfx_core_shatter, cols = 4, rows = 4, totalFrames = 16, durationMs = 380L),
    GLITCH_DETONATE(R.drawable.vfx_glitch_zap, cols = 4, rows = 4, totalFrames = 16, durationMs = 280L),
    MEGA_BLITZ_BURST(R.drawable.vfx_catalyst_blast, cols = 4, rows = 4, totalFrames = 16, durationMs = 450L),
    NODE_ACTIVATE(R.drawable.vfx_matrix_node, cols = 4, rows = 4, totalFrames = 16, durationMs = 300L),
    FRENZY_BOX(R.drawable.vfx_frenzy, cols = 4, rows = 4, totalFrames = 16, durationMs = 400L),
    FEVER_AURA(R.drawable.vfx_overdrive_aura, cols = 4, rows = 4, totalFrames = 16, durationMs = 800L)
}

class SpriteVfxInstance {
    var isActive: Boolean = false
    var type: SpriteVfxType = SpriteVfxType.ROW_LASER
    var startTimeMs: Long = 0L
    val bounds: RectF = RectF()
}

class OneShotSpriteVfxEngine(context: Context) {

    private val bitmapCache = mutableMapOf<SpriteVfxType, Bitmap>()
    private val poolSize = 32
    private val instancePool = Array(poolSize) { SpriteVfxInstance() }

    private val srcRect = Rect()
    private val screenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    init {
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        SpriteVfxType.entries.forEach { vfx ->
            try {
                BitmapFactory.decodeResource(context.resources, vfx.drawableRes, options)?.let {
                    bitmapCache[vfx] = it
                }
            } catch (_: Exception) {}
        }
    }

    fun spawnVfx(type: SpriteVfxType, bounds: RectF, now: Long = SystemClock.uptimeMillis()) {
        val instance = instancePool.firstOrNull { !it.isActive } ?: return
        instance.apply {
            this.isActive = true
            this.type = type
            this.startTimeMs = now
            this.bounds.set(bounds)
        }
    }

    fun hasActiveVfx(): Boolean = instancePool.any { it.isActive }

    fun clearAll() {
        instancePool.forEach { it.isActive = false }
    }

    fun render(canvas: Canvas, now: Long) {
        for (i in 0 until poolSize) {
            val instance = instancePool[i]
            if (!instance.isActive) continue

            val elapsed = now - instance.startTimeMs
            val spec = instance.type
            val bmp = bitmapCache[spec]

            if (elapsed >= spec.durationMs || bmp == null) {
                instance.isActive = false
                continue
            }

            val progress = elapsed.toFloat() / spec.durationMs
            val frameIndex = (progress * spec.totalFrames).toInt().coerceIn(0, spec.totalFrames - 1)

            if (bmp != null) {
                val frameW = bmp.width / spec.cols
                val frameH = bmp.height / spec.rows
                val col = frameIndex % spec.cols
                val row = frameIndex / spec.cols

                srcRect.set(
                    col * frameW,
                    row * frameH,
                    (col + 1) * frameW,
                    (row + 1) * frameH
                )

                canvas.drawBitmap(bmp, srcRect, instance.bounds, screenPaint)
            }
        }
    }

    fun renderLoopingVfx(canvas: Canvas, type: SpriteVfxType, bounds: RectF, now: Long) {
        val bmp = bitmapCache[type] ?: return
        val elapsed = now % type.durationMs
        val progress = elapsed.toFloat() / type.durationMs
        val frameIndex = (progress * type.totalFrames).toInt().coerceIn(0, type.totalFrames - 1)

        val frameW = bmp.width / type.cols
        val frameH = bmp.height / type.rows
        val col = frameIndex % type.cols
        val row = frameIndex / type.cols

        srcRect.set(
            col * frameW,
            row * frameH,
            (col + 1) * frameW,
            (row + 1) * frameH
        )

        canvas.drawBitmap(bmp, srcRect, bounds, screenPaint)
    }
}
