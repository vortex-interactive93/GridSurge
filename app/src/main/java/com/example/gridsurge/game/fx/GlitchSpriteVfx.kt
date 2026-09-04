package com.example.gridsurge.game.fx

import android.content.Context
import android.graphics.*
import com.example.gridsurge.R

class GlitchSpriteVfx(context: Context) {

    private val spriteBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.vfx_infected_overlay)
    private val totalCols = 4
    private val totalRows = 4
    private val totalFrames = 16

    private val frameWidth = spriteBitmap.width / totalCols
    private val frameHeight = spriteBitmap.height / totalRows

    private val srcRect = Rect()
    private val destRect = RectF()

    // Blends pure black away and intensifies neon glow
    private val screenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    /**
     * Draws the animated glitch frame over the target cell.
     * @param now Current uptime in millis.
     * @param cellRect Board coordinates for the tile.
     * @param cellIndex Used as a time offset so multiple infected tiles don't animate in sync.
     */
    fun drawGlitchOverlay(canvas: Canvas, cellRect: RectF, now: Long, cellIndex: Int = 0) {
        // 16 frames cycling every 800ms (~50ms per frame / 20 FPS)
        val frameDurationMs = 50L
        val offsetTime = now + (cellIndex * 130L)
        val currentFrame = ((offsetTime / frameDurationMs) % totalFrames).toInt()

        val col = currentFrame % totalCols
        val row = currentFrame / totalCols

        srcRect.set(
            col * frameWidth,
            row * frameHeight,
            (col + 1) * frameWidth,
            (row + 1) * frameHeight
        )

        // Slight scale expansion (1.12x) so tendrils creep just outside the tile edge
        val pad = cellRect.width() * 0.06f
        destRect.set(
            cellRect.left - pad,
            cellRect.top - pad,
            cellRect.right + pad,
            cellRect.bottom + pad
        )

        canvas.drawBitmap(spriteBitmap, srcRect, destRect, screenPaint)
    }
}
