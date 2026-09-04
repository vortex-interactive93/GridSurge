package com.example.gridsurge.game.engine

import com.example.gridsurge.features.adventure.model.AdventureHazardType
import com.example.gridsurge.game.model.GridCell
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.SpecialBlockType

object PlacementSafetyEngine {

    /**
     * Unified Fit Checker: Validates if any piece in the tray can be placed.
     * Prevents premature "MATRIX LOCKED" game-over.
     */
    fun canAnyPieceBePlaced(
        dockShapes: Array<PolyShape?>,
        engineGrid: IntArray,
        adventureGrid: Array<Array<GridCell>>?,
        hazardGrid: Array<Array<com.example.gridsurge.features.adventure.model.HazardCellState>>? = null
    ): Boolean {
        for (shape in dockShapes) {
            if (shape == null) continue

            // Special bombs ALWAYS fit anywhere on board
            if (shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX ||
                shape.specialType == SpecialBlockType.CATALYST_CROSSHAIR ||
                shape.specialType == SpecialBlockType.NOVA_CORE_EXPLOSION) {
                return true
            }

            // Check every possible anchor position for standard pieces
            val gridSize = 8
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (canPieceFitAt(shape, c, r, engineGrid, adventureGrid, hazardGrid)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun canPieceFitAt(
        shape: PolyShape,
        anchorCol: Int,
        anchorRow: Int,
        engineGrid: IntArray,
        adventureGrid: Array<Array<GridCell>>?,
        hazardGrid: Array<Array<com.example.gridsurge.features.adventure.model.HazardCellState>>?
    ): Boolean {
        for (offset in shape.offsets) {
            val tc = anchorCol + offset.x
            val tr = anchorRow + offset.y

            if (tc !in 0..7 || tr !in 0..7) return false

            // Check occupancy in base engine
            if (engineGrid[tr * 8 + tc] != 0) return false

            // Check occupancy in adventure board (Cores)
            if (adventureGrid != null && adventureGrid[tr][tc].isFilled) return false

            // Check EMP hazards
            if (hazardGrid != null && hazardGrid[tr][tc].hazardType == AdventureHazardType.EMP_LOCK) {
                return false
            }
        }
        return true
    }
}
