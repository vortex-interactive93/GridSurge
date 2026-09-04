package com.example.gridsurge.game.juice

import com.example.gridsurge.game.model.PolyShape

object BoardPressureEvaluator {

    private const val GRID_SIZE = 8

    /**
     * Checks all remaining pieces in dock against the active board.
     * Evaluates in O(N * 64) where N <= 3 dock shapes.
     */
    fun evaluateBoardPressure(
        gridMatrix: IntArray,
        dockShapes: Array<PolyShape?>
    ): DangerLevel {
        var unplaceablePieces = 0
        var activePieces = 0

        for (i in 0 until 3) {
            val shape = dockShapes[i] ?: continue
            activePieces++

            var hasLegalPlacement = false

            // Sweep all 64 potential anchor sockets
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    if (canPlaceShapeAt(gridMatrix, shape, c, r)) {
                        hasLegalPlacement = true
                        break
                    }
                }
                if (hasLegalPlacement) break
            }

            if (!hasLegalPlacement) {
                unplaceablePieces++
            }
        }

        return when {
            activePieces == 0 -> DangerLevel.SAFE
            unplaceablePieces >= 2 -> DangerLevel.CRITICAL
            unplaceablePieces == 1 -> DangerLevel.WARNING
            else -> DangerLevel.SAFE
        }
    }

    private fun canPlaceShapeAt(
        gridMatrix: IntArray,
        shape: PolyShape,
        anchorCol: Int,
        anchorRow: Int
    ): Boolean {
        for (offset in shape.offsets) {
            val targetCol = anchorCol + offset.x
            val targetRow = anchorRow + offset.y

            // Boundary collision check
            if (targetCol !in 0 until GRID_SIZE || targetRow !in 0 until GRID_SIZE) {
                return false
            }

            // Tile occupancy check
            if (gridMatrix[targetRow * GRID_SIZE + targetCol] != 0) {
                return false
            }
        }
        return true
    }
}
