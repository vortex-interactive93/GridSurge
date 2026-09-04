package com.example.gridsurge.game.fx

import android.graphics.Color
import androidx.annotation.ColorInt

enum class VfxType {
    LASER_HORIZONTAL,
    LASER_VERTICAL,
    INTERSECTION_FLARE,
    WAVEFORM_CONDUIT,
    PARTICLE_BURST
}

class LaserBeamVfx {
    var isActive: Boolean = false
        private set

    var type: VfxType = VfxType.LASER_HORIZONTAL
        private set

    var gridIndex: Int = 0 // Row or Column index
    var crossIndex: Int = -1 // For intersection flares (-1 if full beam)
    
    var startX: Float = 0f
    var startY: Float = 0f
    var endX: Float = 0f
    var endY: Float = 0f

    @ColorInt var coreColor: Int = Color.WHITE
    @ColorInt var glowColor: Int = Color.CYAN

    var durationSec: Float = 0.35f
    var elapsedSec: Float = 0f
    var progress: Float = 0f
        private set

    var alpha: Float = 1f
        private set

    var beamThickness: Float = 4f
        private set

    fun spawnBeam(
        vfxType: VfxType,
        index: Int,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        @ColorInt coreHex: Int,
        @ColorInt glowHex: Int,
        duration: Float = 0.35f,
        thickness: Float = 6f
    ) {
        this.type = vfxType
        this.gridIndex = index
        this.crossIndex = -1
        this.startX = x1
        this.startY = y1
        this.endX = x2
        this.endY = y2
        this.coreColor = coreHex
        this.glowColor = glowHex
        this.durationSec = duration
        this.elapsedSec = 0f
        this.progress = 0f
        this.alpha = 1f
        this.beamThickness = thickness
        this.isActive = true
    }

    fun spawnIntersectionFlare(
        gridR: Int,
        gridC: Int,
        cx: Float,
        cy: Float,
        @ColorInt glowHex: Int,
        duration: Float = 0.45f
    ) {
        this.type = VfxType.INTERSECTION_FLARE
        this.gridIndex = gridR
        this.crossIndex = gridC
        this.startX = cx
        this.startY = cy
        this.endX = cx
        this.endY = cy
        this.coreColor = Color.WHITE
        this.glowColor = glowHex
        this.durationSec = duration
        this.elapsedSec = 0f
        this.progress = 0f
        this.alpha = 1f
        this.beamThickness = 12f
        this.isActive = true
    }

    fun update(dt: Float): Boolean {
        if (!isActive) return false

        elapsedSec += dt
        progress = (elapsedSec / durationSec).coerceIn(0f, 1f)

        // Interpolation curve: Rapid flash (0.0 -> 0.15), smooth exponential fade (0.15 -> 1.0)
        alpha = if (progress < 0.15f) {
            progress / 0.15f
        } else {
            val fadeProgress = (progress - 0.15f) / 0.85f
            (1f - fadeProgress) * (1f - fadeProgress)
        }

        if (progress >= 1f || elapsedSec >= durationSec) {
            reset()
            return false
        }
        return true
    }

    fun reset() {
        isActive = false
        elapsedSec = 0f
        progress = 0f
        alpha = 0f
    }
}
