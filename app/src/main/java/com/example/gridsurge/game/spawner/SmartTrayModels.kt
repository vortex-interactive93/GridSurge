package com.example.gridsurge.game.spawner

import com.example.gridsurge.game.model.PolyShape

/**
 * Encapsulates a near-complete row or column that can be completed with targeted solver pieces.
 */
data class LineGapOpportunity(
    val isRow: Boolean,
    val lineIndex: Int,
    val filledCount: Int,
    val missingIndices: IntArray // Up to 2 missing coordinate indices (e.g. cols [2, 3] in Row 4)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LineGapOpportunity
        return isRow == other.isRow && lineIndex == other.lineIndex && missingIndices.contentEquals(other.missingIndices)
    }

    override fun hashCode(): Int {
        var result = isRow.hashCode()
        result = 31 * result + lineIndex
        result = 31 * result + missingIndices.contentHashCode()
        return result
    }
}

/**
 * Real-time spatial telemetry of the 8x8 matrix.
 */
data class BoardPressureMetrics(
    val totalFilledCells: Int,
    val fillPercentage: Float,
    val primedLineCount: Int, // Lines sitting at 6/8 or 7/8 filled
    val maxOpenContiguousBlock: Int // Size of largest open bounding box (e.g. 1, 2, 3)
)
