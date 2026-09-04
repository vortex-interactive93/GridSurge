package com.example.gridsurge.game.model

import android.graphics.RectF

/**
 * Pre-allocated entity for block placement landing pop & shockwave rings.
 */
class PlacementImpactEntity {
    var isActive: Boolean = false
    var centerX: Float = 0f
    var centerY: Float = 0f
    var startTimeMs: Long = 0L
    var durationMs: Long = 200L
    var impactColor: Int = 0xFF00E5FF.toInt()
    var maxRadiusPx: Float = 0f

    fun spawn(x: Float, y: Float, color: Int, maxRadius: Float, now: Long) {
        isActive = true
        centerX = x
        centerY = y
        impactColor = color
        maxRadiusPx = maxRadius
        startTimeMs = now
        durationMs = 200L
    }

    fun getProgress(now: Long): Float {
        if (!isActive) return 1f
        val elapsed = now - startTimeMs
        val progress = elapsed.toFloat() / durationMs.toFloat()
        if (progress >= 1f) {
            isActive = false
            return 1f
        }
        return progress.coerceIn(0f, 1f)
    }
}

/**
 * Pre-allocated laser wavefront beam entity for horizontal/vertical line purges.
 */
class LaserBeamEntity {
    var isActive: Boolean = false
    var isHorizontal: Boolean = false
    var index: Int = 0
    var startTimeMs: Long = 0L
    var durationMs: Long = 180L
    var laserColor: Int = 0xFF00E5FF.toInt()
    val bounds: RectF = RectF()

    fun spawn(isHorizontal: Boolean, index: Int, targetBounds: RectF, color: Int, now: Long) {
        this.isActive = true
        this.isHorizontal = isHorizontal
        this.index = index
        this.bounds.set(targetBounds)
        this.laserColor = color
        this.startTimeMs = now
        this.durationMs = 180L
    }

    fun getProgress(now: Long): Float {
        if (!isActive) return 1f
        val elapsed = now - startTimeMs
        val progress = elapsed.toFloat() / durationMs.toFloat()
        if (progress >= 1f) {
            isActive = false
            return 1f
        }
        return progress.coerceIn(0f, 1f)
    }
}

/**
 * Encapsulates the state of line completion previews during drag.
 */
class LinePreviewState {
    var isValidPlacement: Boolean = false
    var totalLines: Int = 0
    var rowMask: Int = 0
    var colMask: Int = 0
    
    fun hasRow(r: Int) = (rowMask and (1 shl r)) != 0
    fun hasCol(c: Int) = (colMask and (1 shl c)) != 0
    
    fun reset() {
        isValidPlacement = false
        totalLines = 0
        rowMask = 0
        colMask = 0
    }
}

/**
 * Immutable definition of a polyomino block piece.
 */
data class PolyShape(
    val id: String,
    val offsets: List<PolyOffset>, // Immutable offsets to prevent in-place coordinate pollution
    val color: Int,
    val isSpecial: Boolean = false,
    val specialType: SpecialBlockType = SpecialBlockType.NONE,
    val textureResId: Int = 0
) {
    val cols: Int = (offsets.maxOf { it.x } - offsets.minOf { it.x } + 1)
    val rows: Int = (offsets.maxOf { it.y } - offsets.minOf { it.y } + 1)
}

data class PolyOffset(val x: Int, val y: Int)

/**
 * Transient state container for active touch dragging.
 * Decoupled from the authoritative board matrix to prevent ghost leaks.
 */
class DragState {
    var isDragging: Boolean = false
    var shape: PolyShape? = null
    var dockSlotIndex: Int = -1

    // Touch Coordinates in View pixels
    var touchX: Float = 0f
    var touchY: Float = 0f

    // Visual bounds of the floating piece
    val visualPieceBounds = android.graphics.RectF()

    // Calculated candidate grid anchor
    var targetCol: Int = -1
    var targetRow: Int = -1
    var isValidPlacement: Boolean = false

    // Line preview bitmasks
    var rowsToClearMask: Int = 0
    var colsToClearMask: Int = 0
    var totalLines: Int = 0

    // Isolated buffer storing [row0, col0, row1, col1, ...] (Zero GC allocation)
    val projectedCoords = IntArray(32)
    var projectedCount: Int = 0
        private set

    // --- Spring-Back Return State ---
    var isSpringing: Boolean = false
    var springTargetX: Float = 0f
    var springTargetY: Float = 0f
    var scale: Float = 1.0f

    fun addProjectedCell(r: Int, c: Int) {
        if (projectedCount * 2 < projectedCoords.size) {
            projectedCoords[projectedCount * 2] = r
            projectedCoords[projectedCount * 2 + 1] = c
            projectedCount++
        }
    }

    fun clearProjection() {
        projectedCount = 0
        rowsToClearMask = 0
        colsToClearMask = 0
        totalLines = 0
        isValidPlacement = false
    }

    fun reset() {
        isDragging = false
        isSpringing = false
        shape = null
        dockSlotIndex = -1
        targetCol = -1
        targetRow = -1
        isValidPlacement = false
        scale = 1.0f
        visualPieceBounds.setEmpty()
        clearProjection()
    }
}
