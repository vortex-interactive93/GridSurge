package com.example.gridsurge.features.adventure.data

import com.example.gridsurge.features.adventure.model.*

object AdventureCatalog {

    val SECTORS: List<SectorSpec>
        get() = AdventureSectorRegistry.SECTORS.map { pack ->
            val meta = pack.metadata
            val theme = SectorThemeRegistry.getTheme(meta.visualThemeKey)

            SectorSpec(
                sectorId = meta.sectorId,
                codename = meta.sectorName,
                subtitle = meta.subtitle,
                primaryColor = theme.primaryColor,
                secondaryColor = theme.secondaryColor,
                requiredStarsToUnlock = if (meta.sectorId == 1) 0 else (meta.sectorId - 1) * 18,
                backgroundDrawableRes = theme.backgroundRes,
                levels = pack.getAllStages().mapIndexed { idx, stageDef ->
                    val stageInSector = idx + 1
                    val globalStageNumber = (meta.sectorId - 1) * 9 + stageInSector

                    LevelNodeSpec(
                        levelNumber = globalStageNumber,
                        sectorIndex = meta.sectorId,
                        levelInSector = stageInSector,
                        title = stageDef.blueprint.stageName,
                        objective = LevelObjective(
                            type = stageDef.blueprint.objective.type,
                            targetAmount = stageDef.blueprint.objective.targetAmount,
                            maxMovesAllowed = stageDef.benchmarks.moveBudgetStar2 + 10,
                            initialInfectedCoresCount = stageDef.blueprint.initialCores.size,
                            starThresholds = listOf(
                                stageDef.benchmarks.targetScore1Star,
                                stageDef.benchmarks.targetScore2Star,
                                stageDef.benchmarks.targetScore3Star
                            )
                        ),
                        normalizedX = 0.5f + (if (stageInSector % 2 == 0) 0.2f else -0.2f),
                        isBossLevel = stageInSector == pack.totalStages,
                        initialBoardLayout = generateLayoutFromPlacements(stageDef.blueprint.initialCores)
                    )
                }
            )
        }

    private fun generateLayoutFromPlacements(placements: List<CorePlacementSpec>): List<Int> {
        val array = IntArray(64)
        placements.forEach { p ->
            val idx = p.row * 8 + p.col
            if (idx in 0..63) {
                array[idx] = -1
            }
        }
        return array.toList()
    }
}
