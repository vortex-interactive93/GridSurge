package com.example.gridsurge.game.engine

import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.SpecialBlockType
import kotlin.math.*

data class SpecialClearingResult(
    val affectedRowsMask: Int = 0,
    val affectedColsMask: Int = 0,
    val directClearedCellIndices: Set<Int> = emptySet(),
    val isVortexActive: Boolean = false,
    val vortexCenterCol: Int = -1,
    val vortexCenterRow: Int = -1
)

object SpecialBlockSolver {

    private const val GRID_SIZE = 8

    /**
     * Calculates all destroyed rows, columns, and 3x3 spatial zones for hovered or dropped pieces.
     */
    fun evaluateSpecialClear(
        shape: PolyShape,
        anchorCol: Int,
        anchorRow: Int
    ): SpecialClearingResult {
        var rowsMask = 0
        var colsMask = 0
        val cellIndices = mutableSetOf<Int>()

        return when (shape.specialType) {
            SpecialBlockType.CATALYST_CROSSHAIR -> {
                // Every piece tile projects an infinite laser beam across its row and column
                for (offset in shape.offsets) {
                    val c = anchorCol + offset.x
                    val r = anchorRow + offset.y
                    // Strict Clamp to 0..7
                    if (c in 0 until GRID_SIZE && r in 0 until GRID_SIZE) {
                        rowsMask = rowsMask or (1 shl r)
                        colsMask = colsMask or (1 shl c)
                    }
                }
                SpecialClearingResult(
                    affectedRowsMask = rowsMask,
                    affectedColsMask = colsMask
                )
            }

            SpecialBlockType.QUANTUM_WARP_VORTEX, SpecialBlockType.NOVA_CORE_EXPLOSION -> {
                // Obliterates a 3x3 radial area centered on the anchor point
                val radius = 1.5 // Radial distance logic
                for (dy in -2..2) {
                    for (dx in -2..2) {
                        val c = anchorCol + dx
                        val r = anchorRow + dy

                        if (c in 0 until GRID_SIZE && r in 0 until GRID_SIZE) {
                            val dist = sqrt((dx * dx + dy * dy).toDouble())
                            if (dist <= radius) {
                                cellIndices.add(r * GRID_SIZE + c)
                            }
                        }
                    }
                }
                SpecialClearingResult(
                    directClearedCellIndices = cellIndices,
                    isVortexActive = shape.specialType == SpecialBlockType.QUANTUM_WARP_VORTEX,
                    vortexCenterCol = anchorCol,
                    vortexCenterRow = anchorRow
                )
            }

            SpecialBlockType.PRISM_LASER -> {
                // Clears entire row and col of its anchor
                SpecialClearingResult(
                    affectedRowsMask = (1 shl anchorRow),
                    affectedColsMask = (1 shl anchorCol)
                )
            }

            SpecialBlockType.CIRCUIT_CONDUIT -> {
                SpecialClearingResult()
            }

            SpecialBlockType.NONE -> {
                SpecialClearingResult()
            }
        }
    }
}
