package com.example.gridsurge.game.engine

import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.PolyominoCatalog
import com.example.gridsurge.game.model.ShapeBlueprint
import com.example.gridsurge.game.model.SpecialBlockType
import kotlin.random.Random

/**
 * Advanced Spawner with Heuristic Solving.
 * Analyzes the board in real-time to deliver "Calculated Opportunity" pieces.
 */
object CavityAwareTrayGenerator {
    private const val GRID_SIZE = 8
    private const val WARP_COOLDOWN_MOVES = 4
    private var movesSinceLastWarp: Int = WARP_COOLDOWN_MOVES
    private val shapeBag = mutableListOf<PolyShape>()

    private fun refillBag(prng: Random) {
        shapeBag.clear()
        // Pull one representative from each of the major types
        val fullCatalog: List<ShapeBlueprint> = listOf(
            PolyominoCatalog.MONOMINO_1X1,
            PolyominoCatalog.DOMINO.first(),
            PolyominoCatalog.LINE_3.first(),
            PolyominoCatalog.CORNER_L_3.first(),
            PolyominoCatalog.TETRO_I.first(),
            PolyominoCatalog.TETRO_O.first(),
            PolyominoCatalog.TETRO_T.first(),
            PolyominoCatalog.TETRO_L.first(),
            PolyominoCatalog.TETRO_J.first(),
            PolyominoCatalog.TETRO_S.first(),
            PolyominoCatalog.TETRO_Z.first(),
            PolyominoCatalog.PENTO_PLUS.first(),
            PolyominoCatalog.PENTO_U.first(),
            PolyominoCatalog.PENTO_LINE_5.first(),
            PolyominoCatalog.PENTO_W.first(),
            PolyominoCatalog.PENTO_X.first(),
            PolyominoCatalog.PENTO_F.first(),
            PolyominoCatalog.HEAVY_RECT.first(),
            PolyominoCatalog.BIG_CORNER.first()
        )
        shapeBag.addAll(fullCatalog.map { PolyominoCatalog.instantiate(it) }.shuffled(prng))
    }

    fun generateBalancedTray(
        boardGrid: IntArray,
        activeCoreCount: Int,
        comboStreak: Int,
        moveIndex: Int,
        hasCavityCompressor: Boolean = false,
        hasWarpInjector: Boolean = false,
        progressionEngine: com.example.gridsurge.features.adventure.engine.AdventureProgressionEngine? = null,
        prng: Random = Random.Default
    ): List<PolyShape> {
        val tray = mutableListOf<PolyShape>()

        // 1. SLOT 0: THE SAFETY VALVE (Guaranteed to fit)
        tray.add(selectGuaranteedFitPiece(boardGrid, hasCavityCompressor, prng))

        // 2. SLOT 1: THE ENTROPY (Bag Variety)
        if (shapeBag.isEmpty()) refillBag(prng)
        tray.add(shapeBag.removeAt(0))

        // 3. SLOT 2: THE OPPORTUNIST (Tactical Board Analysis)
        val thirdPiece = if (hasWarpInjector && comboStreak >= 2) {
            PolyominoCatalog.instantiateSpecial(SpecialBlockType.QUANTUM_WARP_VORTEX)
        } else {
            evaluateTacticalOpportunity(boardGrid, comboStreak, moveIndex, hasWarpInjector, progressionEngine, prng)
        }
        tray.add(thirdPiece)

        return tray.shuffled(prng)
    }

    /**
     * Scores potential shapes based on how well they "solve" the current board state.
     */
    private fun evaluateTacticalOpportunity(
        board: IntArray,
        combo: Int,
        moveIdx: Int,
        hasWarp: Boolean,
        progressionEngine: com.example.gridsurge.features.adventure.engine.AdventureProgressionEngine?,
        prng: Random
    ): PolyShape {
        // 1. Strict Warp Block Gating
        val shouldSpawnSpecial = progressionEngine?.shouldSpawnQuantumWarp(
            hasWarpInjectorAugment = hasWarp,
            currentCombo = combo,
            boardCongestionRatio = board.count { it != 0 } / 64f,
            prng = prng
        ) ?: run {
            movesSinceLastWarp++
            if (movesSinceLastWarp < WARP_COOLDOWN_MOVES) false
            else if (hasWarp && combo >= 3) {
                movesSinceLastWarp = 0
                true
            } else false
        }

        if (shouldSpawnSpecial) {
            return PolyominoCatalog.instantiateSpecial(SpecialBlockType.QUANTUM_WARP_VORTEX)
        }

        // 2. Heuristic Search: Roll 6 random shapes and pick the "helper"
        val candidates = (PolyominoCatalog.STANDARD_POOL + PolyominoCatalog.TRIOMINOS_AND_SMALL_CORNERS)
            .shuffled(prng).take(6)
        
        var bestBlueprint: ShapeBlueprint = candidates.first()
        var bestScore = -1000f

        for (candidate in candidates) {
            val score = scoreShapeForBoard(candidate.offsets, board)
            if (score > bestScore) {
                bestScore = score
                bestBlueprint = candidate
            }
        }

        return PolyominoCatalog.instantiate(bestBlueprint)
    }

    private fun scoreShapeForBoard(offsets: List<com.example.gridsurge.game.model.PolyOffset>, board: IntArray): Float {
        var maxScore = -100f
        val maxCol = offsets.maxOf { it.x }
        val maxRow = offsets.maxOf { it.y }

        // Sample a few placement points (don't check all 64 to save CPU)
        for (r in 0..(GRID_SIZE - 1 - maxRow)) {
            for (c in 0..(GRID_SIZE - 1 - maxCol)) {
                if (canPlaceAt(offsets, board, r, c)) {
                    var currentPlacementScore = 0f
                    
                    // BONUS: Line Clear Potential
                    if (wouldClearLine(offsets, board, r, c)) {
                        currentPlacementScore += 50f
                    }

                    // BONUS: Edge Adjacency (snug fit)
                    currentPlacementScore += countAdjacency(offsets, board, r, c) * 2f

                    // PENALTY: Creating "Islands" (1x1 holes)
                    if (createsDeadEnd(offsets, board, r, c)) {
                        currentPlacementScore -= 30f
                    }

                    if (currentPlacementScore > maxScore) maxScore = currentPlacementScore
                }
            }
        }
        return maxScore
    }

    private fun canPlaceAt(offsets: List<com.example.gridsurge.game.model.PolyOffset>, board: IntArray, row: Int, col: Int): Boolean {
        for (pt in offsets) {
            val idx = (row + pt.y) * GRID_SIZE + (col + pt.x)
            if (board[idx] != 0) return false
        }
        return true
    }

    private fun wouldClearLine(offsets: List<com.example.gridsurge.game.model.PolyOffset>, board: IntArray, row: Int, col: Int): Boolean {
        val tempBoard = board.copyOf()
        offsets.forEach { pt -> tempBoard[(row + pt.y) * GRID_SIZE + (col + pt.x)] = 1 }
        
        // Check rows
        for (r in 0 until GRID_SIZE) {
            var full = true
            for (c in 0 until GRID_SIZE) {
                if (tempBoard[r * GRID_SIZE + c] == 0) { full = false; break }
            }
            if (full) return true
        }
        // Check cols
        for (c in 0 until GRID_SIZE) {
            var full = true
            for (r in 0 until GRID_SIZE) {
                if (tempBoard[r * GRID_SIZE + c] == 0) { full = false; break }
            }
            if (full) return true
        }
        return false
    }

    private fun countAdjacency(offsets: List<com.example.gridsurge.game.model.PolyOffset>, board: IntArray, row: Int, col: Int): Int {
        var adj = 0
        for (pt in offsets) {
            val r = row + pt.y
            val c = col + pt.x
            // Neighbors
            val neighbors = listOf(r-1 to c, r+1 to c, r to c-1, r to c+1)
            for (n in neighbors) {
                if (n.first !in 0 until GRID_SIZE || n.second !in 0 until GRID_SIZE) {
                    adj++ // Wall adjacency is good
                } else if (board[n.first * GRID_SIZE + n.second] != 0) {
                    adj++ // Block adjacency is good
                }
            }
        }
        return adj
    }

    private fun createsDeadEnd(offsets: List<com.example.gridsurge.game.model.PolyOffset>, board: IntArray, row: Int, col: Int): Boolean {
        val tempBoard = board.copyOf()
        offsets.forEach { pt -> tempBoard[(row + pt.y) * GRID_SIZE + (col + pt.x)] = 1 }
        
        for (idx in 0 until 64) {
            if (tempBoard[idx] == 0) {
                val r = idx / GRID_SIZE
                val c = idx % GRID_SIZE
                val neighbors = listOf(r-1 to c, r+1 to c, r to c-1, r to c+1)
                val blocked = neighbors.count { n ->
                    n.first !in 0 until GRID_SIZE || n.second !in 0 until GRID_SIZE || tempBoard[n.first * GRID_SIZE + n.second] != 0
                }
                if (blocked == 4) return true // 1x1 hole created
            }
        }
        return false
    }

    private fun selectGuaranteedFitPiece(board: IntArray, hasCavityCompressor: Boolean, prng: Random): PolyShape {
        val candidates: List<ShapeBlueprint> = if (hasCavityCompressor) {
            PolyominoCatalog.MONOS_AND_DOMINOS + PolyominoCatalog.TRIOMINOS_AND_SMALL_CORNERS
        } else {
            PolyominoCatalog.STANDARD_POOL + PolyominoCatalog.TRIOMINOS_AND_SMALL_CORNERS
        }

        val fitting = candidates.filter { canPieceFit(it.offsets, board) }
        return if (fitting.isNotEmpty()) {
            PolyominoCatalog.instantiate(fitting.random(prng))
        } else {
            PolyominoCatalog.instantiate(PolyominoCatalog.MONOMINO_1X1)
        }
    }

    private fun canPieceFit(offsets: List<com.example.gridsurge.game.model.PolyOffset>, board: IntArray): Boolean {
        val maxCol = offsets.maxOf { it.x }
        val maxRow = offsets.maxOf { it.y }
        for (r in 0..(GRID_SIZE - 1 - maxRow)) {
            for (c in 0..(GRID_SIZE - 1 - maxCol)) {
                if (canPlaceAt(offsets, board, r, c)) return true
            }
        }
        return false
    }
}
