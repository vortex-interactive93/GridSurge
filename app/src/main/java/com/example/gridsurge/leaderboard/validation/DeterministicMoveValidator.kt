package com.example.gridsurge.leaderboard.validation

import com.example.gridsurge.core.ComboStateManager
import com.example.gridsurge.game.model.PolyShape
import com.example.gridsurge.leaderboard.model.GameModeType
import com.example.gridsurge.leaderboard.model.MatchReplayEnvelope
import kotlin.random.Random

sealed class ValidationResult {
    data class Valid(val groundTruthScore: Long, val movesProcessed: Int) : ValidationResult()
    data class Invalid(val reason: String, val failedMoveNumber: Int) : ValidationResult()
}

class DeterministicMoveValidator(
    private val shapeCatalog: List<PolyShape>
) {
    private val gridSize = 8
    private val shapeMap: Map<Short, PolyShape> = shapeCatalog.associateBy { it.id.hashCode().toShort() }

    fun validateReplay(envelope: MatchReplayEnvelope): ValidationResult {
        val prng = Random(envelope.seed)
        val gridMatrix = IntArray(gridSize * gridSize)
        val comboManager = ComboStateManager(maxGraceMoves = 2)

        var simulatedScore = 0L
        var totalLines = 0
        var maxCombo = 0

        // Active Spawner Tray (3 slots)
        val activeTray = arrayOfNulls<PolyShape>(3)
        fun replenishTray() {
            for (i in 0 until 3) {
                val nextIndex = prng.nextInt(shapeCatalog.size)
                activeTray[i] = shapeCatalog[nextIndex]
            }
        }

        replenishTray()

        for ((index, move) in envelope.moves.withIndex()) {
            // 1. Verify Tray Slot Bounds & Availability
            if (move.trayIndex !in 0..2) {
                return ValidationResult.Invalid("Illegal tray index: ${move.trayIndex}", index)
            }
            val expectedShape = activeTray[move.trayIndex.toInt()]
                ?: return ValidationResult.Invalid("Tray slot ${move.trayIndex} was empty on move $index", index)

            // 2. Verify Shape Identity
            val catalogShape = shapeMap[move.shapeId]
                ?: return ValidationResult.Invalid("Unknown shape ID: ${move.shapeId}", index)

            if (expectedShape.id != catalogShape.id) {
                return ValidationResult.Invalid("Shape mismatch in tray slot ${move.trayIndex}. Expected ${expectedShape.id}, got ${catalogShape.id}", index)
            }

            // 3. Verify Grid Placement Legality (Collision & Bounds)
            for (offset in catalogShape.offsets) {
                val c = move.anchorCol + offset.x
                val r = move.anchorRow + offset.y
                if (c !in 0 until gridSize || r !in 0 until gridSize) {
                    return ValidationResult.Invalid("Out of bounds placement at ($c, $r) on move $index", index)
                }
                if (gridMatrix[r * gridSize + c] != 0) {
                    return ValidationResult.Invalid("Tile collision at ($c, $r) on move $index", index)
                }
            }

            // 4. Commit Placement to Headless Matrix
            catalogShape.offsets.forEach { offset ->
                val c = move.anchorCol + offset.x
                val r = move.anchorRow + offset.y
                gridMatrix[r * gridSize + c] = catalogShape.color
            }

            // Consume Tray Item
            activeTray[move.trayIndex.toInt()] = null

            // 5. Evaluate Completed Rows and Columns
            var clearedRowsMask = 0
            var clearedColsMask = 0
            var moveClearedLines = 0

            for (r in 0 until gridSize) {
                var full = true
                for (c in 0 until gridSize) {
                    if (gridMatrix[r * gridSize + c] == 0) { full = false; break }
                }
                if (full) {
                    clearedRowsMask = clearedRowsMask or (1 shl r)
                    moveClearedLines++
                }
            }

            for (c in 0 until gridSize) {
                var full = true
                for (r in 0 until gridSize) {
                    if (gridMatrix[r * gridSize + c] == 0) { full = false; break }
                }
                if (full) {
                    clearedColsMask = clearedColsMask or (1 shl c)
                    moveClearedLines++
                }
            }

            // 6. Clear Matrix Cells
            for (r in 0 until gridSize) {
                if ((clearedRowsMask and (1 shl r)) != 0) {
                    for (c in 0 until gridSize) gridMatrix[r * gridSize + c] = 0
                }
            }
            for (c in 0 until gridSize) {
                if ((clearedColsMask and (1 shl c)) != 0) {
                    for (r in 0 until gridSize) gridMatrix[r * gridSize + c] = 0
                }
            }

            // 7. Calculate Deterministic Score & Multipliers
            val comboResult = comboManager.onMoveCommitted(moveClearedLines)
            maxCombo = maxOf(maxCombo, comboResult.currentStreak)
            totalLines += moveClearedLines

            val moveBaseScore = when (envelope.mode) {
                GameModeType.TIME_BLITZ.storageKey -> {
                    val base = if (moveClearedLines > 0) (moveClearedLines * 150L) * moveClearedLines + (comboResult.currentStreak * 75L) else 10L
                    base
                }
                else -> {
                    val base = (moveClearedLines * 100L) * (if (moveClearedLines > 1) moveClearedLines else 1)
                    val comboBonus = if (comboResult.currentStreak > 1) (comboResult.currentStreak * 50L) else 0L
                    base + comboBonus
                }
            }
            simulatedScore += moveBaseScore

            // Replenish Tray if all 3 consumed
            if (activeTray.all { it == null }) {
                replenishTray()
            }
        }

        // 8. Strict Verification of Score and Multipliers
        return if (simulatedScore == envelope.claimedScore) {
            ValidationResult.Valid(groundTruthScore = simulatedScore, movesProcessed = envelope.moves.size)
        } else {
            ValidationResult.Invalid("Score mismatch! Claimed: ${envelope.claimedScore}, Simulated: $simulatedScore", envelope.moves.size)
        }
    }
}
