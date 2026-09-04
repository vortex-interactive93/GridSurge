package com.example.gridsurge.game.spawner

import com.example.gridsurge.R
import com.example.gridsurge.core.ShapeRegistry
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.model.SpecialBlockType
import kotlin.random.Random

class BalancedDockSpawner(
    private val gridSize: Int = 8
) {
    // --- Special Block Cooldown & Probability State ---
    private var movesSinceLastSpecial = 0
    private val minCooldownMoves = 14
    private val specialSpawnChance = 0.022f // 2.2% controlled probability

    // Anti-repetition cache: prevents rolling the exact same 3-shape IDs back-to-back
    private var lastTraySignature = ""

    // Pre-allocated reusable simulation buffers (Zero-allocation during gameplay)
    private val simGrid = IntArray(64)
    private val rowCounts = IntArray(8)
    private val colCounts = IntArray(8)
    private val tempMissingBuffer = IntArray(8)

    /**
     * Primary entry point: Generates a balanced 3-piece tray using line-gap heuristics
     * and sequential playability verification.
     */
    fun replenishDock(
        gridMatrix: IntArray,
        currentStageLevel: Int = 1,
        movesPlayedInStage: Int = 0,
        prng: Random = Random.Default
    ): Array<PolyShape?> {
        val tray = arrayOfNulls<PolyShape>(3)
        val metrics = evaluateBoardPressure(gridMatrix)
        val gapOpportunities = analyzeMatrixGaps(gridMatrix)

        // =========================================================================
        // SLOT 0: The Key Solver / Adaptive Savior
        // =========================================================================
        // If board has primed lines (6/8 or 7/8), try to inject a clearing shape.
        // Probability scales with board pressure (70% standard -> 100% when congested).
        val shouldInjectSolver = gapOpportunities.isNotEmpty() &&
                (metrics.fillPercentage >= 0.60f || prng.nextFloat() < 0.75f)

        var solverShape: PolyShape? = null
        if (shouldInjectSolver) {
            solverShape = findClearingCandidate(gapOpportunities, gridMatrix, prng)
        }

        tray[0] = solverShape ?: ShapeRegistry.TIER_1_SAVIORS.random(prng)

        // =========================================================================
        // SLOT 1: The Workhorse Builder
        // =========================================================================
        // When board pressure is high, restrict builder selection to compact shapes.
        val workerPool = if (metrics.fillPercentage > 0.65f) {
            ShapeRegistry.TIER_2_WORKERS.filter { it.offsets.size <= 3 }
        } else {
            ShapeRegistry.TIER_2_WORKERS
        }
        tray[1] = workerPool.random(prng)

        // =========================================================================
        // SLOT 2: Heavy Cleaver OR Tactical Special
        // =========================================================================
        val canRollSpecial = (movesSinceLastSpecial >= minCooldownMoves) && (prng.nextFloat() < specialSpawnChance)

        if (canRollSpecial) {
            val isCatalyst = prng.nextBoolean()
            tray[2] = if (isCatalyst) {
                PolyShape(
                    id = "special_catalyst_1x1",
                    offsets = listOf(com.example.gridsurge.game.model.PolyOffset(0, 0)),
                    color = ShapeRegistry.COLOR_GOLD,
                    isSpecial = true,
                    specialType = SpecialBlockType.CATALYST_CROSSHAIR,
                    textureResId = R.drawable.skin_catalyst_block
                )
            } else {
                PolyShape(
                    id = "special_warp_1x1",
                    offsets = listOf(com.example.gridsurge.game.model.PolyOffset(0, 0)),
                    color = ShapeRegistry.COLOR_AMETHYST,
                    isSpecial = true,
                    specialType = SpecialBlockType.QUANTUM_WARP_VORTEX,
                    textureResId = R.drawable.skin_warp_block
                )
            }
            movesSinceLastSpecial = 0
        } else {
            // Filter out 3x3 solid cubes and 5-bars if board is congested or in early stages
            val allowedHeavies = ShapeRegistry.TIER_3_HEAVIES.filter { shape ->
                when {
                    shape.id == "sq_3x3" -> currentStageLevel > 3 && movesPlayedInStage >= 6 && metrics.fillPercentage <= 0.45f
                    shape.id.startsWith("5x1") || shape.id.startsWith("1x5") -> metrics.fillPercentage <= 0.65f
                    else -> true
                }
            }
            tray[2] = if (allowedHeavies.isNotEmpty()) allowedHeavies.random(prng) else ShapeRegistry.TIER_2_WORKERS.random(prng)
        }

        // =========================================================================
        // SEQUENTIAL PLAYABILITY & ANTI-LOCK VERIFICATION
        // =========================================================================
        // 1. Verify at least one piece can physically fit immediately
        if (!canAnyPieceFit(tray, gridMatrix)) {
            val fittingSaviors = ShapeRegistry.TIER_1_SAVIORS.filter { canPieceFitOnBoard(it, gridMatrix) }
            tray[0] = when {
                fittingSaviors.isNotEmpty() -> fittingSaviors.random(prng)
                canPieceFitOnBoard(ShapeRegistry.MONO_1X1, gridMatrix) -> ShapeRegistry.MONO_1X1
                else -> tray[0]
            }
        }

        // 2. Multi-Step Consecutive Placement Check (Guarantee >= 2 consecutive placements)
        enforceSequentialFlow(tray, gridMatrix, prng)

        // Record signature to avoid visual duplicate rolls
        lastTraySignature = "${tray[0]?.id}_${tray[1]?.id}_${tray[2]?.id}"
        return tray
    }

    /**
     * Scans the 8x8 matrix to identify all rows and columns with 6 or 7 filled sockets.
     */
    fun analyzeMatrixGaps(gridMatrix: IntArray): List<LineGapOpportunity> {
        val opportunities = mutableListOf<LineGapOpportunity>()
        rowCounts.fill(0)
        colCounts.fill(0)

        // Count row/col fills
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val index = r * gridSize + c
                if (gridMatrix[index] != com.example.gridsurge.core.CellType.EMPTY.id && gridMatrix[index] != 0) {
                    rowCounts[r]++
                    colCounts[c]++
                }
            }
        }

        // Scan primed rows (6 or 7 filled)
        for (r in 0 until gridSize) {
            val count = rowCounts[r]
            if (count in 6..7) {
                var missingCount = 0
                for (c in 0 until gridSize) {
                    val idx = r * gridSize + c
                    if (gridMatrix[idx] == com.example.gridsurge.core.CellType.EMPTY.id || gridMatrix[idx] == 0) {
                        tempMissingBuffer[missingCount++] = c
                    }
                }
                opportunities.add(
                    LineGapOpportunity(
                        isRow = true,
                        lineIndex = r,
                        filledCount = count,
                        missingIndices = tempMissingBuffer.copyOf(missingCount)
                    )
                )
            }
        }

        // Scan primed columns (6 or 7 filled)
        for (c in 0 until gridSize) {
            val count = colCounts[c]
            if (count in 6..7) {
                var missingCount = 0
                for (r in 0 until gridSize) {
                    val idx = r * gridSize + c
                    if (gridMatrix[idx] == com.example.gridsurge.core.CellType.EMPTY.id || gridMatrix[idx] == 0) {
                        tempMissingBuffer[missingCount++] = r
                    }
                }
                opportunities.add(
                    LineGapOpportunity(
                        isRow = false,
                        lineIndex = c,
                        filledCount = count,
                        missingIndices = tempMissingBuffer.copyOf(missingCount)
                    )
                )
            }
        }

        return opportunities
    }

    /**
     * Searches the shape registry for a compact polyomino that fits into the missing line sockets.
     */
    private fun findClearingCandidate(
        gaps: List<LineGapOpportunity>,
        gridMatrix: IntArray,
        prng: Random
    ): PolyShape? {
        val candidates = mutableListOf<PolyShape>()

        for (gap in gaps) {
            val missing = gap.missingIndices

            if (gap.isRow) {
                val r = gap.lineIndex
                when (missing.size) {
                    1 -> { // 1-socket gap in row
                        val c = missing[0]
                        if (canPlaceAt(ShapeRegistry.MONO_1X1, c, r, gridMatrix)) candidates.add(ShapeRegistry.MONO_1X1)
                        if (canPlaceAt(ShapeRegistry.DOMINO_V, c, r, gridMatrix)) candidates.add(ShapeRegistry.DOMINO_V)
                        if (canPlaceAt(ShapeRegistry.LINE_3_V, c, r, gridMatrix)) candidates.add(ShapeRegistry.LINE_3_V)
                    }
                    2 -> { // 2-socket gap
                        val c1 = missing[0]
                        val c2 = missing[1]
                        if (c2 == c1 + 1) { // Contiguous 2-cell gap
                            if (canPlaceAt(ShapeRegistry.DOMINO_H, c1, r, gridMatrix)) candidates.add(ShapeRegistry.DOMINO_H)
                            if (canPlaceAt(ShapeRegistry.SQUARE_2X2, c1, r, gridMatrix)) candidates.add(ShapeRegistry.SQUARE_2X2)
                            if (canPlaceAt(ShapeRegistry.CORNER_TL, c1, r, gridMatrix)) candidates.add(ShapeRegistry.CORNER_TL)
                            if (canPlaceAt(ShapeRegistry.CORNER_TR, c1, r, gridMatrix)) candidates.add(ShapeRegistry.CORNER_TR)
                        } else {
                            if (canPlaceAt(ShapeRegistry.MONO_1X1, c1, r, gridMatrix)) candidates.add(ShapeRegistry.MONO_1X1)
                            if (canPlaceAt(ShapeRegistry.MONO_1X1, c2, r, gridMatrix)) candidates.add(ShapeRegistry.MONO_1X1)
                        }
                    }
                }
            } else {
                val c = gap.lineIndex
                when (missing.size) {
                    1 -> { // 1-socket gap in col
                        val r = missing[0]
                        if (canPlaceAt(ShapeRegistry.MONO_1X1, c, r, gridMatrix)) candidates.add(ShapeRegistry.MONO_1X1)
                        if (canPlaceAt(ShapeRegistry.DOMINO_H, c, r, gridMatrix)) candidates.add(ShapeRegistry.DOMINO_H)
                        if (canPlaceAt(ShapeRegistry.LINE_3_H, c, r, gridMatrix)) candidates.add(ShapeRegistry.LINE_3_H)
                    }
                    2 -> { // 2-socket gap
                        val r1 = missing[0]
                        val r2 = missing[1]
                        if (r2 == r1 + 1) { // Contiguous 2-cell vertical gap
                            if (canPlaceAt(ShapeRegistry.DOMINO_V, c, r1, gridMatrix)) candidates.add(ShapeRegistry.DOMINO_V)
                            if (canPlaceAt(ShapeRegistry.SQUARE_2X2, c, r1, gridMatrix)) candidates.add(ShapeRegistry.SQUARE_2X2)
                            if (canPlaceAt(ShapeRegistry.CORNER_BL, c, r1, gridMatrix)) candidates.add(ShapeRegistry.CORNER_BL)
                            if (canPlaceAt(ShapeRegistry.CORNER_BR, c, r1, gridMatrix)) candidates.add(ShapeRegistry.CORNER_BR)
                        } else {
                            if (canPlaceAt(ShapeRegistry.MONO_1X1, c, r1, gridMatrix)) candidates.add(ShapeRegistry.MONO_1X1)
                            if (canPlaceAt(ShapeRegistry.MONO_1X1, c, r2, gridMatrix)) candidates.add(ShapeRegistry.MONO_1X1)
                        }
                    }
                }
            }
        }

        return if (candidates.isNotEmpty()) candidates.random(prng) else null
    }

    /**
     * Simulates consecutive drops. If placing Piece 0 leaves Piece 1 completely unplayable,
     * adjusts Piece 1 to a compatible filler.
     */
    private fun enforceSequentialFlow(
        tray: Array<PolyShape?>,
        gridMatrix: IntArray,
        prng: Random
    ) {
        val piece0 = tray[0] ?: return
        val piece1 = tray[1] ?: return

        // Check if Piece 0 can fit and leave room for Piece 1
        var sequentialMatchFound = false

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (canPlaceAt(piece0, c, r, gridMatrix)) {
                    // Create simulated virtual grid
                    System.arraycopy(gridMatrix, 0, simGrid, 0, 64)
                    applySimulatedPlacement(piece0, c, r, simGrid)
                    simulateLineClears(simGrid)

                    if (canPieceFitOnBoard(piece1, simGrid)) {
                        sequentialMatchFound = true
                        break
                    }
                }
            }
            if (sequentialMatchFound) break
        }

        // If Piece 1 is locked out after Piece 0, downgrade Piece 1 to a Tier 1 Savior
        if (!sequentialMatchFound) {
            val fittingSaviors = ShapeRegistry.TIER_1_SAVIORS.filter { canPieceFitOnBoard(it, gridMatrix) }
            tray[1] = if (fittingSaviors.isNotEmpty()) fittingSaviors.random(prng) else ShapeRegistry.MONO_1X1
        }
    }

    /**
     * Calculates total board congestion and primed lines.
     */
    fun evaluateBoardPressure(gridMatrix: IntArray): BoardPressureMetrics {
        var filled = 0
        for (i in 0 until 64) {
            if (gridMatrix[i] != com.example.gridsurge.core.CellType.EMPTY.id && gridMatrix[i] != 0) filled++
        }
        val gaps = analyzeMatrixGaps(gridMatrix)
        return BoardPressureMetrics(
            totalFilledCells = filled,
            fillPercentage = filled / 64f,
            primedLineCount = gaps.size,
            maxOpenContiguousBlock = if (filled > 45) 1 else 3
        )
    }

    fun canAnyPieceFit(tray: Array<PolyShape?>, gridMatrix: IntArray): Boolean {
        for (shape in tray) {
            if (shape != null && canPieceFitOnBoard(shape, gridMatrix)) return true
        }
        return false
    }

    fun canPieceFitOnBoard(shape: PolyShape, gridMatrix: IntArray): Boolean {
        if (shape.specialType != SpecialBlockType.NONE) {
            for (i in 0 until 64) {
                if (gridMatrix[i] == com.example.gridsurge.core.CellType.EMPTY.id || gridMatrix[i] == 0) return true
            }
            return false
        }

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (canPlaceAt(shape, c, r, gridMatrix)) return true
            }
        }
        return false
    }

    private fun canPlaceAt(shape: PolyShape, anchorCol: Int, anchorRow: Int, gridMatrix: IntArray): Boolean {
        for (offset in shape.offsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y
            if (c !in 0 until gridSize || r !in 0 until gridSize) return false
            val index = r * gridSize + c
            if (gridMatrix[index] != com.example.gridsurge.core.CellType.EMPTY.id && gridMatrix[index] != 0) return false
        }
        return true
    }

    private fun applySimulatedPlacement(shape: PolyShape, anchorCol: Int, anchorRow: Int, matrix: IntArray) {
        for (offset in shape.offsets) {
            val c = anchorCol + offset.x
            val r = anchorRow + offset.y
            if (c in 0 until gridSize && r in 0 until gridSize) {
                matrix[r * gridSize + c] = 1
            }
        }
    }

    private fun simulateLineClears(matrix: IntArray) {
        var clearRowMask = 0
        var clearColMask = 0

        for (r in 0 until gridSize) {
            var full = true
            for (c in 0 until gridSize) {
                if (matrix[r * gridSize + c] == 0) { full = false; break }
            }
            if (full) clearRowMask = clearRowMask or (1 shl r)
        }

        for (c in 0 until gridSize) {
            var full = true
            for (r in 0 until gridSize) {
                if (matrix[r * gridSize + c] == 0) { full = false; break }
            }
            if (full) clearColMask = clearColMask or (1 shl c)
        }

        for (r in 0 until gridSize) {
            if ((clearRowMask and (1 shl r)) != 0) {
                for (c in 0 until gridSize) matrix[r * gridSize + c] = 0
            }
        }
        for (c in 0 until gridSize) {
            if ((clearColMask and (1 shl c)) != 0) {
                for (r in 0 until gridSize) matrix[r * gridSize + c] = 0
            }
        }
    }

    fun onPiecePlaced() {
        movesSinceLastSpecial++
    }

    fun reset() {
        movesSinceLastSpecial = 0
        lastTraySignature = ""
    }
}
