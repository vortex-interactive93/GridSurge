package com.example.gridsurge.game.engine

import com.example.gridsurge.game.model.PolyOffset
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.ShapeBlueprint

/**
 * High-performance 8x8 Bitboard engine for sub-microsecond placement feasibility testing.
 * Uses ULong (64-bit) to represent the grid state.
 */
object BitboardFeasibilityEngine {

    /**
     * Converts an 8x8 IntArray grid to a 64-bit ULong bitmask.
     * 1 = Occupied/Obstacle, 0 = Empty.
     */
    fun calculateBoardMask(grid: IntArray): ULong {
        var mask = 0uL
        for (i in 0 until 64) {
            if (grid[i] != 0) {
                mask = mask or (1uL shl i)
            }
        }
        return mask
    }

    /**
     * Checks if a shape fits anywhere on the board using bitwise operations.
     */
    fun canPlaceShape(boardMask: ULong, shapeOffsets: List<PolyOffset>): Boolean {
        // Find top-left relative bounds
        val minX = shapeOffsets.minOf { it.x }
        val minY = shapeOffsets.minOf { it.y }
        val maxX = shapeOffsets.maxOf { it.x }
        val maxY = shapeOffsets.maxOf { it.y }
        
        val width = maxX - minX + 1
        val height = maxY - minY + 1
        
        // Re-calculate mask relative to (0,0) if necessary, 
        // but for now we calculate it on the fly or pass pre-calculated.
        var shapeMask = 0uL
        for (offset in shapeOffsets) {
            val bitIndex = ((offset.y - minY) * 8) + (offset.x - minX)
            shapeMask = shapeMask or (1uL shl bitIndex)
        }

        val limitX = 8 - width
        val limitY = 8 - height

        for (y in 0..limitY) {
            for (x in 0..limitX) {
                val shift = (y * 8) + x
                val shiftedMask = shapeMask shl shift
                if ((boardMask and shiftedMask) == 0uL) {
                    return true
                }
            }
        }
        return false
    }
}
