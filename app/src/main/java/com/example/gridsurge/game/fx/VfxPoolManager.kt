package com.example.gridsurge.game.fx

import android.graphics.Canvas

class VfxPoolManager(
    private val maxBeams: Int = 32,
    private val maxFlares: Int = 16
) {
    private val beamPool = Array(maxBeams) { LaserBeamVfx() }
    private val flarePool = Array(maxFlares) { LaserBeamVfx() }

    fun spawnHorizontalLaser(
        row: Int,
        x1: Float,
        y: Float,
        x2: Float,
        coreColor: Int,
        glowColor: Int,
        duration: Float = 0.35f
    ) {
        val beam = getAvailableEntity(beamPool) ?: return
        beam.spawnBeam(
            vfxType = VfxType.LASER_HORIZONTAL,
            index = row,
            x1 = x1,
            y1 = y,
            x2 = x2,
            y2 = y,
            coreHex = coreColor,
            glowHex = glowColor,
            duration = duration,
            thickness = 5f
        )
    }

    fun spawnVerticalLaser(
        col: Int,
        x: Float,
        y1: Float,
        y2: Float,
        coreColor: Int,
        glowColor: Int,
        duration: Float = 0.35f
    ) {
        val beam = getAvailableEntity(beamPool) ?: return
        beam.spawnBeam(
            vfxType = VfxType.LASER_VERTICAL,
            index = col,
            x1 = x,
            y1 = y1,
            x2 = x,
            y2 = y2,
            coreHex = coreColor,
            glowHex = glowColor,
            duration = duration,
            thickness = 5f
        )
    }

    fun spawnIntersection(
        row: Int,
        col: Int,
        cx: Float,
        cy: Float,
        glowColor: Int,
        duration: Float = 0.45f
    ) {
        val flare = getAvailableEntity(flarePool) ?: return
        flare.spawnIntersectionFlare(row, col, cx, cy, glowColor, duration)
    }

    fun update(dt: Float) {
        if (dt <= 0f) return // Frozen during pause

        for (i in 0 until maxBeams) {
            if (beamPool[i].isActive) {
                beamPool[i].update(dt)
            }
        }
        for (i in 0 until maxFlares) {
            if (flarePool[i].isActive) {
                flarePool[i].update(dt)
            }
        }
    }

    fun clearAll() {
        for (i in 0 until maxBeams) {
            beamPool[i].reset()
        }
        for (i in 0 until maxFlares) {
            flarePool[i].reset()
        }
    }

    val hasActiveEffects: Boolean
        get() {
            for (i in 0 until maxBeams) {
                if (beamPool[i].isActive) return true
            }
            for (i in 0 until maxFlares) {
                if (flarePool[i].isActive) return true
            }
            return false
        }

    fun render(canvas: Canvas, renderer: VfxCanvasRenderer, virtualTime: Float) {
        renderer.renderEffects(canvas, beamPool, flarePool, virtualTime)
    }

    private fun getAvailableEntity(pool: Array<LaserBeamVfx>): LaserBeamVfx? {
        for (i in pool.indices) {
            if (!pool[i].isActive) return pool[i]
        }
        // Force recycled oldest effect if saturated
        var oldest = pool[0]
        for (i in 1 until pool.size) {
            if (pool[i].elapsedSec > oldest.elapsedSec) {
                oldest = pool[i]
            }
        }
        oldest.reset()
        return oldest
    }
}
