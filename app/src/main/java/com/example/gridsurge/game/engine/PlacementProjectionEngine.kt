package com.example.gridsurge.game.engine

import com.example.gridsurge.features.adventure.model.AdventureHazardType
import com.example.gridsurge.features.adventure.model.HazardCellState
import com.example.gridsurge.core.CellType
import com.example.gridsurge.game.model.DragState

class PlacementProjectionEngine(private val gridSize: Int = 8) {

    fun calculateProjection(
        dragState: DragState,
        gridMatrix: IntArray,
        isSpecial: Boolean = false,
        hazardMatrix: Array<Array<HazardCellState>>? = null
    ) {
        dragState.clearProjection()
        val shape = dragState.shape ?: return
        val anchorCol = dragState.targetCol
        val anchorRow = dragState.targetRow

        // 1. Boundary & Collision Scan
        var fits = true
        for (offset in shape.offsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y

            if (c !in 0 until gridSize || r !in 0 until gridSize) {
                fits = false
                break
            }

            val idx = r * gridSize + c
            if (!isSpecial && gridMatrix[idx] != CellType.EMPTY.id) {
                fits = false
                break
            }

            if (!isSpecial && hazardMatrix != null && hazardMatrix[r][c].hazardType == AdventureHazardType.EMP_LOCK) {
                fits = false
                break
            }
        }

        dragState.isValidPlacement = fits
        if (!fits) return

        // 2. Populate isolated coordinate projection
        for (offset in shape.offsets) {
            dragState.addProjectedCell(anchorRow + offset.y, anchorCol + offset.x)
        }

        // 3. Compute Projected Line Clear Masks
        var rowMask = 0
        var colMask = 0
        var totalLines = 0

        for (r in 0 until gridSize) {
            var full = true
            for (c in 0 until gridSize) {
                val inShape = shape.offsets.any { anchorCol + it.x == c && anchorRow + it.y == r }
                if (!inShape && gridMatrix[r * gridSize + c] == CellType.EMPTY.id) {
                    full = false
                    break
                }
            }
            if (full) {
                rowMask = rowMask or (1 shl r)
                totalLines++
            }
        }

        for (c in 0 until gridSize) {
            var full = true
            for (r in 0 until gridSize) {
                val inShape = shape.offsets.any { anchorCol + it.x == c && anchorRow + it.y == r }
                if (!inShape && gridMatrix[r * gridSize + c] == CellType.EMPTY.id) {
                    full = false
                    break
                }
            }
            if (full) {
                colMask = colMask or (1 shl c)
                totalLines++
            }
        }

        dragState.rowsToClearMask = rowMask
        dragState.colsToClearMask = colMask
        dragState.totalLines = totalLines
    }
}
