package com.example.gridsurge.armory.model

data class MatrixCell(
    val row: Int,
    val col: Int,
    val pieceId: Int, // 1 = Active Dropped Piece, 2 = Settled Grid Matrix
    val isAnchor: Boolean = false
)

object ArmoryMatrixConfig {
    // 4x4 Preview Layout consisting of two distinct puzzle pieces
    val SIMULATION_CELLS = listOf(
        // Piece A: 4-Block Polyomino (Active Highlight Hero Piece)
        MatrixCell(row = 0, col = 1, pieceId = 1),
        MatrixCell(row = 0, col = 2, pieceId = 1),
        MatrixCell(row = 1, col = 1, pieceId = 1),
        MatrixCell(row = 1, col = 2, pieceId = 1),

        // Piece B: Polyomino Cluster (Settled Matrix)
        MatrixCell(row = 1, col = 0, pieceId = 2),
        MatrixCell(row = 2, col = 0, pieceId = 2),
        MatrixCell(row = 2, col = 1, pieceId = 2),
        MatrixCell(row = 2, col = 3, pieceId = 2)
    )
}
