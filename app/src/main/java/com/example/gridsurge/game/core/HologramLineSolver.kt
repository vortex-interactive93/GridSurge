package com.example.gridsurge.game.core

import com.example.gridsurge.game.model.LinePreviewState

object HologramLineSolver {
    fun evaluateCandidatePlacement(
        gridMatrix: IntArray,
        shapeOffsets: List<com.example.gridsurge.game.model.PolyOffset>,
        anchorCol: Int,
        anchorRow: Int,
        outState: LinePreviewState,
        isSpecial: Boolean = false,
        hazardMatrix: Array<Array<com.example.gridsurge.features.adventure.model.HazardCellState>>? = null
    ) {
        outState.reset()
        
        // 1. Check basic boundary and collision validity
        for (offset in shapeOffsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y
            
            // Special blocks can be dropped on occupied cells for detonation effects
            if (c !in 0..7 || r !in 0..7 || (!isSpecial && gridMatrix[r * 8 + c] != 0)) {
                outState.isValidPlacement = false
                return
            }

            // Hazard EMP Lock Check
            if (!isSpecial && hazardMatrix != null && hazardMatrix[r][c].hazardType == com.example.gridsurge.features.adventure.model.AdventureHazardType.EMP_LOCK) {
                outState.isValidPlacement = false
                return
            }
        }
        
        outState.isValidPlacement = true
        
        // 2. Identify which rows/cols would clear
        var rowMask = 0
        var colMask = 0
        
        for (r in 0 until 8) {
            var full = true
            for (c in 0 until 8) {
                val isInShape = shapeOffsets.any { anchorCol + it.x == c && anchorRow + it.y == r }
                if (!isInShape && gridMatrix[r * 8 + c] == 0) {
                    full = false
                    break
                }
            }
            if (full) {
                rowMask = rowMask or (1 shl r)
                outState.totalLines++
            }
        }
        
        for (c in 0 until 8) {
            var full = true
            for (r in 0 until 8) {
                val isInShape = shapeOffsets.any { anchorCol + it.x == c && anchorRow + it.y == r }
                if (!isInShape && gridMatrix[r * 8 + c] == 0) {
                    full = false
                    break
                }
            }
            if (full) {
                colMask = colMask or (1 shl c)
                outState.totalLines++
            }
        }
        
        outState.rowMask = rowMask
        outState.colMask = colMask
    }
}
