package com.example.gridsurge.game.fx

import android.graphics.Color
import android.graphics.RectF
import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.game.render.FloatingScoreManager
import com.example.gridsurge.game.particle.CyberParticleSystem

class JuiceCoordinator(
    private val density: Float,
    private val vfxPool: VfxPoolManager,
    private val juiceFx: JuiceFxEngine,
    private val spriteVfxEngine: OneShotSpriteVfxEngine,
    private val particleSystem: CyberParticleSystem,
    private val scorePopupManager: FloatingScoreManager,
    private val warpVortexFx: WarpVortexFxEngine
) {
    var boardRect = RectF()
    var cellSize = 0f
    var cellSpacing = 0f

    // Callback to trigger shake in the View
    var onTriggerShake: ((Float) -> Unit)? = null

    fun onPiecePlaced(placedCoords: List<Pair<Int, Int>>, color: Int) {
        placedCoords.forEach { (col, row) ->
            val cx = boardRect.left + cellSpacing + (col + 0.5f) * (cellSize + cellSpacing)
            val cy = boardRect.top + cellSpacing + (row + 0.5f) * (cellSize + cellSpacing)
            juiceFx.spawnLandingShockwave(cx, cy, color)
        }
    }

    fun onLinesCleared(result: ClearResult, comboStreak: Int, now: Long) {
        if (result.totalLines >= 3) {
            spriteVfxEngine.spawnVfx(SpriteVfxType.MEGA_BLITZ_BURST, boardRect, now)
        }

        // Trigger white-hot flash and vaporize particles on cleared cells
        val rowsMask = result.clearedRows.fold(0) { mask, r -> mask or (1 shl r) }
        val colsMask = result.clearedCols.fold(0) { mask, c -> mask or (1 shl c) }
        juiceFx.triggerLineImpactFlash(rowsMask, colsMask)
        result.clearedRows.forEach { r ->
            for (c in 0 until 8) {
                val cx = boardRect.left + cellSpacing + (c + 0.5f) * (cellSize + cellSpacing)
                val cy = boardRect.top + cellSpacing + (r + 0.5f) * (cellSize + cellSpacing)
                spawnBurstParticles(cx, cy, Color.parseColor("#00E5FF"), count = 3)
            }
        }
        result.clearedCols.forEach { c ->
            for (r in 0 until 8) {
                val cx = boardRect.left + cellSpacing + (c + 0.5f) * (cellSize + cellSpacing)
                val cy = boardRect.top + cellSpacing + (r + 0.5f) * (cellSize + cellSpacing)
                spawnBurstParticles(cx, cy, Color.parseColor("#EA80FC"), count = 3)
            }
        }

        result.clearedRows.forEach { r ->
            val yCenter = boardRect.top + cellSpacing + (r + 0.5f) * (cellSize + cellSpacing)
            val h = cellSize * 1.6f
            val rect = RectF(boardRect.left - 12f * density, yCenter - h / 2f, boardRect.right + 12f * density, yCenter + h / 2f)
            val type = if (comboStreak >= 3) SpriteVfxType.ROW_LIGHTNING else SpriteVfxType.ROW_LASER
            spriteVfxEngine.spawnVfx(type, rect, now)
        }

        result.clearedCols.forEach { c ->
            val xCenter = boardRect.left + cellSpacing + (c + 0.5f) * (cellSize + cellSpacing)
            val w = cellSize * 1.6f
            val topBound = maxOf(boardRect.top, 85f * density)
            val rect = RectF(xCenter - w / 2f, topBound, xCenter + w / 2f, boardRect.bottom + 14f * density)
            spriteVfxEngine.spawnVfx(SpriteVfxType.COL_LIGHTNING, rect, now)
        }

        if (result.clearedRows.isNotEmpty() && result.clearedCols.isNotEmpty()) {
            val size = cellSize * 2.8f
            result.clearedRows.forEach { r ->
                result.clearedCols.forEach { c ->
                    val cx = boardRect.left + cellSpacing + (c + 0.5f) * (cellSize + cellSpacing)
                    val cy = boardRect.top + cellSpacing + (r + 0.5f) * (cellSize + cellSpacing)
                    val rect = RectF(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f)
                    spriteVfxEngine.spawnVfx(SpriteVfxType.CROSS_BURST, rect, now)
                }
            }
        }
    }

    fun spawnLaserVfx(rowsMask: Int, colsMask: Int, color: Int) {
        val laserGlow = color
        val laserCore = Color.WHITE

        for (r in 0 until 8) {
            if ((rowsMask and (1 shl r)) != 0) {
                val y = boardRect.top + cellSpacing + (r + 0.5f) * (cellSize + cellSpacing)
                vfxPool.spawnHorizontalLaser(r, boardRect.left, y, boardRect.right, laserCore, laserGlow)
            }
        }
        for (c in 0 until 8) {
            if ((colsMask and (1 shl c)) != 0) {
                val x = boardRect.left + cellSpacing + (c + 0.5f) * (cellSize + cellSpacing)
                vfxPool.spawnVerticalLaser(c, x, boardRect.top, boardRect.bottom, laserCore, laserGlow)
            }
        }
        // Intersection flares
        for (r in 0 until 8) {
            if ((rowsMask and (1 shl r)) != 0) {
                for (c in 0 until 8) {
                    if ((colsMask and (1 shl c)) != 0) {
                        val cx = boardRect.left + cellSpacing + (c + 0.5f) * (cellSize + cellSpacing)
                        val cy = boardRect.top + cellSpacing + (r + 0.5f) * (cellSize + cellSpacing)
                        vfxPool.spawnIntersection(r, c, cx, cy, Color.parseColor("#FF0055"))
                    }
                }
            }
        }
    }

    fun spawnPopup(x: Float, y: Float, text: String, color: Int, now: Long) {
        scorePopupManager.spawnPopup(x, y, text, color, now)
    }

    fun triggerShake(intensity: Float) {
        onTriggerShake?.invoke(intensity)
    }

    fun spawnBurstParticles(x: Float, y: Float, color: Int, count: Int = 15) {
        juiceFx.spawnBurstParticles(x, y, color, count)
    }

    fun spawnExplosion(x: Float, y: Float, color: Int, count: Int = 40) {
        particleSystem.spawnLineExplosion(x - cellSize, y - cellSize, cellSize * 2, cellSize * 2, color, count)
    }

    fun triggerGlitchFlash(intensity: Float = 1.0f) {
        juiceFx.triggerCatalystGlitchFlash(intensity)
    }

    fun spawnCorruptionSpread(fromIdx: Int, toIdx: Int) {
        val fx = boardRect.left + cellSpacing + (fromIdx % 8 + 0.5f) * (cellSize + cellSpacing)
        val fy = boardRect.top + cellSpacing + (fromIdx / 8 + 0.5f) * (cellSize + cellSpacing)
        val tx = boardRect.left + cellSpacing + (toIdx % 8 + 0.5f) * (cellSize + cellSpacing)
        val ty = boardRect.top + cellSpacing + (toIdx / 8 + 0.5f) * (cellSize + cellSpacing)
        juiceFx.spawnCorruptionSpread(fx, fy, tx, ty)
    }

    fun spawnBurstParticlesForCell(index: Int, color: Int, count: Int = 15) {
        val x = boardRect.left + cellSpacing + (index % 8 + 0.5f) * (cellSize + cellSpacing)
        val y = boardRect.top + cellSpacing + (index / 8 + 0.5f) * (cellSize + cellSpacing)
        juiceFx.spawnBurstParticles(x, y, color, count)
    }

    fun getCellCenter(index: Int, out: RectF) {
        val x = boardRect.left + cellSpacing + (index % 8 + 0.5f) * (cellSize + cellSpacing)
        val y = boardRect.top + cellSpacing + (index / 8 + 0.5f) * (cellSize + cellSpacing)
        out.set(x, y, x, y)
    }
}
