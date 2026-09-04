package com.example.gridsurge.game.logic

import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.core.GridEngine
import com.example.gridsurge.game.engine.BitboardFeasibilityEngine
import com.example.gridsurge.game.fx.JuiceCoordinator
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.game.spawner.ClassicPieceSpawner

class ClassicModeController(
    private val engine: GridEngine,
    private val juiceCoordinator: JuiceCoordinator
) {
    val spawner = ClassicPieceSpawner()

    var linesClearedTotal: Int = 0
        private set

    var surgeStreakMultiplier: Int = 1
        private set

    var preSeededInitialCount: Int = 0
        private set

    var preSeededRemainingCount: Int = 0
        private set

    var isPurgeBountyClaimed: Boolean = false
        private set

    var currentSeedType: ClassicSeedGenerator.SeedType = ClassicSeedGenerator.SeedType.PRISTINE
        private set

    private val preSeededTileMask = Array(8) { BooleanArray(8) { false } }

    fun initializeMatch() {
        linesClearedTotal = 0
        surgeStreakMultiplier = 1
        isPurgeBountyClaimed = false
        spawner.reset()

        val seedResult = ClassicSeedGenerator.generateSeed()
        currentSeedType = seedResult.seedType
        preSeededInitialCount = seedResult.initialBlockCount
        preSeededRemainingCount = seedResult.initialBlockCount

        // Copy discrete tile tracking mask & populate grid
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                preSeededTileMask[r][c] = seedResult.preSeededMask[r][c]
                engine.setGridValue(c, r, seedResult.grid[r][c])
            }
        }
    }

    fun processMove(result: ClearResult): Long {
        val clearedRows = result.clearedRows
        val clearedCols = result.clearedCols
        val totalLines = result.totalLines

        if (totalLines > 0) {
            linesClearedTotal += totalLines
            
            // Increment Surge Streak Multiplier up to 5x
            surgeStreakMultiplier = (surgeStreakMultiplier + 1).coerceAtMost(5)

            // Exponential Scoring
            val baseLineScore = when (totalLines) {
                1 -> 100L
                2 -> 300L
                3 -> 700L
                4 -> 1500L
                else -> 2000L
            }

            val totalLineScore = baseLineScore * surgeStreakMultiplier
            engine.score += totalLineScore

            // --- Track Cleared Pre-Seeded Tiles ---
            for (r in clearedRows) {
                for (c in 0 until 8) {
                    if (preSeededTileMask[r][c]) {
                        preSeededTileMask[r][c] = false
                        preSeededRemainingCount = (preSeededRemainingCount - 1).coerceAtLeast(0)
                    }
                }
            }
            for (c in clearedCols) {
                for (r in 0 until 8) {
                    if (preSeededTileMask[r][c]) {
                        preSeededTileMask[r][c] = false
                        preSeededRemainingCount = (preSeededRemainingCount - 1).coerceAtLeast(0)
                    }
                }
            }

            // --- Cross-Clear Slag / Intersection Shatter Rule ---
            if (clearedRows.isNotEmpty() && clearedCols.isNotEmpty()) {
                var crossShatterTriggered = false
                for (r in clearedRows) {
                    for (c in clearedCols) {
                        val valAtIntersect = engine.getGridValue(c, r)
                        if (valAtIntersect > 1) { // Multi-hit / Slag block
                            engine.setGridValue(c, r, 0) // Shatter instantly in 1 turn
                            crossShatterTriggered = true
                        }
                    }
                }
                val label = if (crossShatterTriggered) "CROSS SHATTER!" else "CROSS CLEAR!"
                juiceCoordinator.spawnPopup(0f, 0f, label, 0xFF00FF66.toInt(), 0L)
            }

            // --- Grid Purge Bounty Check ---
            if (preSeededInitialCount > 0 && preSeededRemainingCount == 0 && !isPurgeBountyClaimed) {
                isPurgeBountyClaimed = true
                engine.score += 2000L
                surgeStreakMultiplier = (surgeStreakMultiplier + 1).coerceAtMost(5)
                juiceCoordinator.spawnPopup(0f, 0f, "+2,000 PTS // PURGE BOUNTY!", 0xFFFFD600.toInt(), 0L)
            }
        } else {
            // Reset surge streak multiplier on non-clearing move
            surgeStreakMultiplier = 1
        }

        return engine.score
    }

    /**
     * Executes the Revive Protocol strictly inside controller business logic:
     * 1. Clears 4x4 central area on GridEngine.
     * 2. Clears pre-seeded tile tracking flags.
     * 3. Resets combo streak to 1x for leaderboard fairness.
     * 4. Discards stalled tray and generates 3 fresh, viable pieces.
     */
    fun executeEmpRevive(): Array<PolyShape?> {
        for (r in 2..5) {
            for (c in 2..5) {
                if (preSeededTileMask[r][c]) {
                    preSeededTileMask[r][c] = false
                    preSeededRemainingCount = (preSeededRemainingCount - 1).coerceAtLeast(0)
                }
                engine.setGridValue(c, r, 0)
                engine.setCellColor(c, r, 0)
            }
        }

        resetSurgeStreak()
        engine.comboManager.reset()

        val occ = engine.getOccupiedRatio()
        val boardMask = BitboardFeasibilityEngine.calculateBoardMask(engine.getGridArray())
        val freshTray = spawner.nextTray(occ, surgeStreakMultiplier, boardMask)

        // Check if EMP clearance triggered Grid Purge Bounty
        if (preSeededInitialCount > 0 && preSeededRemainingCount == 0 && !isPurgeBountyClaimed) {
            isPurgeBountyClaimed = true
            engine.score += 2000L
            surgeStreakMultiplier = (surgeStreakMultiplier + 1).coerceAtMost(5)
            juiceCoordinator.spawnPopup(0f, 0f, "+2,000 PTS // PURGE BOUNTY!", 0xFFFFD600.toInt(), 0L)
        }

        return freshTray
    }

    fun resetSurgeStreak() {
        surgeStreakMultiplier = 1
    }

    fun reset() {
        linesClearedTotal = 0
        surgeStreakMultiplier = 1
        preSeededInitialCount = 0
        preSeededRemainingCount = 0
        isPurgeBountyClaimed = false
        currentSeedType = ClassicSeedGenerator.SeedType.PRISTINE
        for (r in 0 until 8) for (c in 0 until 8) preSeededTileMask[r][c] = false
        spawner.reset()
    }
}
