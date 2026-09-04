package com.example.gridsurge.game.engine

import com.example.gridsurge.core.CellType
import com.example.gridsurge.core.ClearResult
import com.example.gridsurge.core.GridEngine
import com.example.gridsurge.features.adventure.core.AdventureBoardManager
import com.example.gridsurge.features.adventure.model.AdventureRunState
import com.example.gridsurge.features.adventure.model.AugmentType
import com.example.gridsurge.features.adventure.model.ObjectiveType
import com.example.gridsurge.game.model.CoreIntegrity

data class OverclockActionPayload(
    val secondaryLaserRows: List<Int> = emptyList(),
    val secondaryLaserCols: List<Int> = emptyList(),
    val kineticHitCoreCoords: List<Pair<Int, Int>> = emptyList(),
    val pointsAwarded: Long = 0L,
    val tilesSynthesized: Int = 0
)

object OverclockExecutionEngine {

    fun resolvePostClearAugments(
        result: ClearResult,
        runState: AdventureRunState,
        engine: GridEngine,
        adventureBoard: AdventureBoardManager,
        elapsedSec: Int,
        placedCoords: List<Pair<Int, Int>> = emptyList()
    ): OverclockActionPayload {
        if (result.totalLines <= 0) return OverclockActionPayload()

        val secondaryRows = mutableListOf<Int>()
        val secondaryCols = mutableListOf<Int>()
        val kineticHits = mutableListOf<Pair<Int, Int>>()
        var bonusPoints = 0L
        var extraTilesPurged = 0

        val placedCols = placedCoords.map { it.first }.toSet()
        val placedRows = placedCoords.map { it.second }.toSet()

        // 1. CARDINAL OVERCLOCK: Secondary Perpendicular Lasers from Placed Block
        if (runState.hasAugment(AugmentType.CARDINAL_OVERCLOCK)) {
            // For cleared rows -> fire vertical lasers down columns perpendicular to placed piece
            if (result.clearedRows.isNotEmpty()) {
                val targetCols = if (placedCols.isNotEmpty()) placedCols else setOf(3, 4)
                for (c in targetCols) {
                    if (c in 0..7) {
                        secondaryCols.add(c)
                        for (r in 0 until 8) {
                            val cell = adventureBoard.grid[r][c]
                            if (engine.getGridValue(c, r) != CellType.EMPTY.id && !cell.isCore) {
                                engine.setGridValue(c, r, CellType.EMPTY.id)
                                engine.setCellColor(c, r, 0)
                                cell.isFilled = false
                                cell.blockColor = 0
                                bonusPoints += 150L
                                extraTilesPurged++
                            } else if (cell.isCore && cell.coreIntegrity != CoreIntegrity.DESTROYED && !cell.isInvulnerable) {
                                val wasDestroyed = adventureBoard.damageCore(r, c, elapsedSec, isWarp = false)
                                if (wasDestroyed) {
                                    engine.setGridValue(c, r, CellType.EMPTY.id)
                                    engine.setCellColor(c, r, 0)
                                }
                                kineticHits.add(r to c)
                                bonusPoints += 500L
                            }
                        }
                    }
                }
            }

            // For cleared cols -> fire horizontal lasers across rows perpendicular to placed piece
            if (result.clearedCols.isNotEmpty()) {
                val targetRows = if (placedRows.isNotEmpty()) placedRows else setOf(3, 4)
                for (r in targetRows) {
                    if (r in 0..7) {
                        secondaryRows.add(r)
                        for (c in 0 until 8) {
                            val cell = adventureBoard.grid[r][c]
                            if (engine.getGridValue(c, r) != CellType.EMPTY.id && !cell.isCore) {
                                engine.setGridValue(c, r, CellType.EMPTY.id)
                                engine.setCellColor(c, r, 0)
                                cell.isFilled = false
                                cell.blockColor = 0
                                bonusPoints += 150L
                                extraTilesPurged++
                            } else if (cell.isCore && cell.coreIntegrity != CoreIntegrity.DESTROYED && !cell.isInvulnerable) {
                                val wasDestroyed = adventureBoard.damageCore(r, c, elapsedSec, isWarp = false)
                                if (wasDestroyed) {
                                    engine.setGridValue(c, r, CellType.EMPTY.id)
                                    engine.setCellColor(c, r, 0)
                                }
                                kineticHits.add(r to c)
                                bonusPoints += 500L
                            }
                        }
                    }
                }
            }
        }

        // 2. KINETIC BURST: Multi-line Clears Deal 1 Damage to ALL Active Cores
        if (runState.hasAugment(AugmentType.KINETIC_BURST) && result.totalLines >= 2) {
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val cell = adventureBoard.grid[r][c]
                    if (cell.isCore && cell.coreIntegrity != CoreIntegrity.DESTROYED && !cell.isInvulnerable) {
                        val wasDestroyed = adventureBoard.damageCore(r, c, elapsedSec, isWarp = false)
                        if (wasDestroyed) {
                            engine.setGridValue(c, r, CellType.EMPTY.id)
                            engine.setCellColor(c, r, 0)
                        }
                        kineticHits.add(r to c)
                        bonusPoints += 500L
                    }
                }
            }
        }

        // 3. Update Chroma Synthesis Objective Count from Laser Clears
        if (extraTilesPurged > 0 && adventureBoard.activeBlueprint?.objective?.type == ObjectiveType.CHROMA_SYNTHESIS) {
            val targetAmount = adventureBoard.activeBlueprint?.objective?.targetAmount ?: 45
            adventureBoard.synthesisCount = (adventureBoard.synthesisCount + extraTilesPurged).coerceAtMost(targetAmount)
        }

        return OverclockActionPayload(
            secondaryLaserRows = secondaryRows.distinct(),
            secondaryLaserCols = secondaryCols.distinct(),
            kineticHitCoreCoords = kineticHits,
            pointsAwarded = bonusPoints,
            tilesSynthesized = extraTilesPurged
        )
    }
}
