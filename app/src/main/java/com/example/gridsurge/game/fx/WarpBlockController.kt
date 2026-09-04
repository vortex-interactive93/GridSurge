package com.example.gridsurge.game.fx

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.view.animation.AccelerateInterpolator
import com.example.gridsurge.core.CellType
import com.example.gridsurge.core.GridEngine
import com.example.gridsurge.game.model.DetonationEffect
import com.example.gridsurge.game.model.DetonationTarget
import com.example.gridsurge.game.model.SpecialBlockType
import com.example.gridsurge.game.render.BlockTextureCache
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class WarpBlockController(
    private val density: Float,
    private val textureCache: BlockTextureCache
) {
    private val matrix = Matrix()
    private val shockwaveEmitter = WarpShockwaveEmitter(density)

    private var isAnimating = false
    private var progress = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var cellSize = 0f
    private var cellSpacing = 0f

    private val suckedTiles = mutableListOf<CapturedTile>()
    private val strainedCores = mutableListOf<CapturedTile>()
    private var centerWarpColor = 0

    private val suctionInterpolator = AccelerateInterpolator(1.8f)
    private var shockwaveTriggered = false
    private var activeThemeKey = ""

    private class CapturedTile(
        val color: Int,
        val startX: Float,
        val startY: Float,
        val isCore: Boolean = false,
        val gridX: Int = 0,
        val gridY: Int = 0
    )

    var onAnimationComplete: ((Int, Int) -> Unit)? = null
    var onPlaySound: ((String) -> Unit)? = null
    var onTriggerHaptic: (() -> Unit)? = null

    fun isVortexAnimating(): Boolean = isAnimating

    fun startImplosion(
        gridX: Int,
        gridY: Int,
        cx: Float,
        cy: Float,
        size: Float,
        spacing: Float,
        engine: GridEngine,
        warpColor: Int,
        themeKey: String
    ) {
        centerX = cx
        centerY = cy
        cellSize = size
        cellSpacing = spacing
        centerWarpColor = warpColor
        activeThemeKey = themeKey
        suckedTiles.clear()
        strainedCores.clear()
        shockwaveTriggered = false

        // 1. Resolve Detonation Targets via Engine
        val targets = engine.resolveWarpDetonation(gridX, gridY)
        
        for (target in targets) {
            val tx = target.col
            val ty = target.row
            val value = engine.getGridValue(tx, ty)
            val color = engine.getCellColor(tx, ty)
            
            val sx = boardX(tx, size, spacing, cx, gridX)
            val sy = boardY(ty, size, spacing, cy, gridY)
            val safeColor = if (color != 0) color else warpColor
            
            val tile = CapturedTile(safeColor, sx, sy, CellType.isCore(value), tx, ty)
            
            if (target.effect == DetonationEffect.SUCTION) {
                suckedTiles.add(tile)
                // Clear cell state from engine immediately for sucked tiles
                engine.setGridValue(tx, ty, CellType.EMPTY.id)
                engine.setCellColor(tx, ty, 0)
            } else {
                strainedCores.add(tile)
            }
        }

        // 2. Drive Implosion Animation Loop
        isAnimating = true
        onPlaySound?.invoke("vacuum_whoosh")
        onTriggerHaptic?.invoke()

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 480L
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                if (progress >= 0.75f && !shockwaveTriggered) {
                    shockwaveTriggered = true
                    shockwaveEmitter.trigger(centerX, centerY, cellSize)
                    onPlaySound?.invoke("snap_pop")
                    onTriggerHaptic?.invoke()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                    suckedTiles.clear()
                    strainedCores.clear()
                    onAnimationComplete?.invoke(gridX, gridY)
                }
            })
            start()
        }
    }

    private fun boardX(tx: Int, size: Float, spacing: Float, cx: Float, gx: Int): Float =
        cx + (tx - gx) * (size + spacing)

    private fun boardY(ty: Int, size: Float, spacing: Float, cy: Float, gy: Int): Float =
        cy + (ty - gy) * (size + spacing)

    fun draw(canvas: Canvas, now: Long) {
        if (!isAnimating && !shockwaveEmitter.draw(canvas, now)) return

        if (isAnimating) {
            val t = progress
            val it = suctionInterpolator.getInterpolation(t)

            // 1. Draw Sucked Tiles (Swirling into singularity)
            for (tile in suckedTiles) {
                val swirlFactor = 1.4f * PI.toFloat()
                val angle = it * swirlFactor

                val dx = centerX - tile.startX
                val dy = centerY - tile.startY

                val curX = tile.startX + dx * it
                val curY = tile.startY + dy * it

                val s = sin(angle)
                val c = cos(angle)
                val rx = (curX - centerX) * c - (curY - centerY) * s + centerX
                val ry = (curX - centerX) * s + (curY - centerY) * c + centerY

                val scale = (1f - it).coerceAtLeast(0.01f)
                val alpha = (255 * (1f - it)).toInt().coerceIn(0, 255)

                matrix.reset()
                matrix.postTranslate(-cellSize / 2f, -cellSize / 2f)

                val stretch = 1f + it * 0.6f
                val compress = (1f - it).coerceAtLeast(0.1f)

                val moveAngle = (atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI).toFloat()
                matrix.postRotate(moveAngle)
                matrix.postScale(stretch * scale, compress * scale)
                matrix.postRotate(-moveAngle)
                matrix.postTranslate(rx, ry)

                textureCache.drawCellWithMatrix(
                    canvas = canvas,
                    matrix = matrix,
                    themeKey = activeThemeKey,
                    tintColor = tile.color,
                    alpha = alpha,
                    specialType = SpecialBlockType.NONE,
                    now = now
                )
            }

            // 2. Draw Strained Cores (Anchored with rattle effect)
            for (core in strainedCores) {
                val rattleX = (Math.random().toFloat() - 0.5f) * 8f * it * density
                val rattleY = (Math.random().toFloat() - 0.5f) * 8f * it * density
                
                matrix.reset()
                matrix.postTranslate(-cellSize / 2f, -cellSize / 2f)
                // Slight scale pulse to indicate strain
                val pulse = 1f + 0.1f * sin(t * 20f)
                matrix.postScale(pulse, pulse)
                matrix.postTranslate(core.startX + rattleX, core.startY + rattleY)

                textureCache.drawCellWithMatrix(
                    canvas = canvas,
                    matrix = matrix,
                    themeKey = activeThemeKey,
                    tintColor = core.color,
                    alpha = 255,
                    specialType = SpecialBlockType.NONE,
                    now = now
                )
            }

            // Draw Central Singularity Core
            drawSingularity(canvas, t, now)
        }

        shockwaveEmitter.draw(canvas, now)
    }

    private fun drawSingularity(canvas: Canvas, t: Float, now: Long) {
        val scale: Float
        val rotation: Float
        val alpha: Int

        if (t < 0.75f) {
            val p = t / 0.75f
            scale = 1f + 0.20f * sin(p * PI.toFloat())
            rotation = t * 360f
            alpha = 255
        } else {
            val p = (t - 0.75f) / 0.25f
            scale = 1.20f * (1f - p)
            rotation = 360f + p * 720f
            alpha = (255 * (1f - p)).toInt().coerceIn(0, 255)
        }

        matrix.reset()
        matrix.postTranslate(-cellSize / 2f, -cellSize / 2f)
        matrix.postRotate(rotation)
        matrix.postScale(scale, scale)
        matrix.postTranslate(centerX, centerY)

        textureCache.drawCellWithMatrix(
            canvas = canvas,
            matrix = matrix,
            themeKey = activeThemeKey,
            tintColor = centerWarpColor,
            alpha = alpha,
            specialType = SpecialBlockType.QUANTUM_WARP_VORTEX,
            now = now
        )
    }
}
