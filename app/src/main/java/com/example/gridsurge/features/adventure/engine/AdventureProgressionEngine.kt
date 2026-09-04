package com.example.gridsurge.features.adventure.engine

import com.example.gridsurge.features.adventure.model.AdventureHazardType
import com.example.gridsurge.features.adventure.model.AdventureLevelBlueprint
import com.example.gridsurge.features.adventure.model.BossPhaseState
import com.example.gridsurge.features.adventure.model.BossThreatState
import com.example.gridsurge.features.adventure.model.HazardCellState

class AdventureProgressionEngine {

    companion object {
        const val GRID_SIZE = 8
    }

    var bossState = BossThreatState()
        private set

    val hazardGrid: Array<Array<HazardCellState>> = Array(GRID_SIZE) { Array(GRID_SIZE) { HazardCellState() } }
    private var isHazardSystemEnabled: Boolean = false
    private var hazardInterval: Int = 0
    private var movesCounter: Int = 0

    fun initializeStage(blueprint: AdventureLevelBlueprint, isBoss: Boolean) {
        movesCounter = 0
        isHazardSystemEnabled = isBoss || blueprint.hasSectorHazards
        hazardInterval = if (isBoss) 4 else blueprint.hazardIntervalMoves
        clearHazards()

        bossState = if (isBoss) {
            BossThreatState(
                bossId = "SECTOR_${blueprint.sectorId}_BOSS",
                phase = BossPhaseState.SHIELDED,
                maxHp = 4,
                currentHp = 4,
                pylonsRemaining = 4,
                movesUntilOvercharge = hazardInterval,
                activeTetherTargetCoords = listOf(1 to 1, 1 to 6, 6 to 1, 6 to 6)
            )
        } else {
            BossThreatState(phase = BossPhaseState.DORMANT)
        }
    }

    /**
     * Ticks player turn. Strictly returns emptyList() if hazards are disabled for this stage.
     */
    fun onPlayerTurnCompleted(board: Array<IntArray>): List<Pair<Int, Int>> {
        if (!isHazardSystemEnabled || hazardInterval <= 0) {
            return emptyList()
        }

        movesCounter++
        val affectedCoords = mutableListOf<Pair<Int, Int>>()

        // Only process overcharge/hazard spawns if interval is met
        if (movesCounter % hazardInterval == 0) {
            var deployed = 0
            for (r in 2..5) {
                for (c in 2..5) {
                    if (board[r][c] == 0 && hazardGrid[r][c].hazardType == AdventureHazardType.NONE && deployed < 1) {
                        hazardGrid[r][c] = HazardCellState(
                            hazardType = AdventureHazardType.EMP_LOCK,
                            countdownMoves = 3
                        )
                        affectedCoords.add(r to c)
                        deployed++
                    }
                }
            }
        }
        return affectedCoords
    }

    fun clearHazards() {
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                hazardGrid[r][c] = HazardCellState()
            }
        }
    }

    /**
     * Resolves Pylon & Boss Damage. Warp bombs cannot hit shielded bosses directly.
     */
    fun registerCoreHit(row: Int, col: Int, isWarpDetonation: Boolean): Boolean {
        if (bossState.phase == BossPhaseState.SHIELDED) {
            val isPylon = bossState.activeTetherTargetCoords.contains(row to col)
            if (isPylon) {
                val remainingPylons = Math.max(0, bossState.pylonsRemaining - 1)
                val updatedTethers = bossState.activeTetherTargetCoords.filterNot { it.first == row && it.second == col }
                val nextPhase = if (remainingPylons == 0) BossPhaseState.VULNERABLE else BossPhaseState.SHIELDED
                bossState = bossState.copy(
                    pylonsRemaining = remainingPylons,
                    phase = nextPhase,
                    activeTetherTargetCoords = updatedTethers
                )
                return true
            }
            // Boss center cannot be damaged while shielded
            return false
        } else if (bossState.phase == BossPhaseState.VULNERABLE) {
            val damage = if (isWarpDetonation) 1 else 2
            val nextHp = Math.max(0, bossState.currentHp - damage)
            bossState = bossState.copy(
                currentHp = nextHp,
                phase = if (nextHp <= 0) BossPhaseState.DEFEATED else BossPhaseState.VULNERABLE
            )
            return true
        } else if (bossState.phase == BossPhaseState.DORMANT) {
            // Non-boss levels: Always allow core hits
            return true
        }
        return false
    }

    fun shouldSpawnQuantumWarp(
        hasWarpInjectorAugment: Boolean,
        currentCombo: Int,
        boardCongestionRatio: Float,
        prng: kotlin.random.Random = kotlin.random.Random.Default
    ): Boolean {
        // Implementation from previous turn or Master Refactor
        // For now using Master Refactor logic
        val canSpawn = when {
            hasWarpInjectorAugment && currentCombo >= 3 -> true
            boardCongestionRatio >= 0.85f && prng.nextFloat() < 0.25f -> true
            currentCombo >= 4 && prng.nextFloat() < 0.40f -> true
            else -> false
        }
        return canSpawn
    }
}
