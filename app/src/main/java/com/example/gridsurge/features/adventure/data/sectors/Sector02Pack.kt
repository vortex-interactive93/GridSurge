package com.example.gridsurge.features.adventure.data.sectors

import com.example.gridsurge.features.adventure.domain.model.*
import com.example.gridsurge.features.adventure.domain.provider.BaseSectorPack
import com.example.gridsurge.features.adventure.model.*

object Sector02Pack : BaseSectorPack(
    metadata = SectorMetadata(
        sectorId = 2,
        sectorName = "SOLAR FOUNDRY",
        subtitle = "THERMAL OVERLOAD",
        visualThemeKey = "THEME_SOLAR_FOUNDRY",
        totalStages = 9,
        rewardTitle = "SOLAR EMBLEM",
        rewardStarGrant = 8,
        baseDifficultyRating = 2
    ),
    stages = listOf(
        // Stage 1 (Global 10)
        StageDefinition(
            stageId = StageId(2, 1),
            blueprint = StageBlueprint(
                stageName = "THERMAL IGNITION",
                directive = "Detonate 2 Solar Crucibles and clear 6 lines before thermal overload.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 SOLAR CRUCIBLES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1500,
                targetScore2Star = 3000,
                targetScore3Star = 5000,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 35,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Mastery: Clear 2+ lines in a single drop"
                )
            )
        ),
        // Stage 2 (Global 11)
        StageDefinition(
            stageId = StageId(2, 2),
            blueprint = StageBlueprint(
                stageName = "PRESSURE VENTING",
                directive = "Complete 5 full line clears while managing thermal exhaust.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.LINE_CLEANSE,
                    title = "5 LINES CLEARED",
                    targetAmount = 5,
                    star3TimeSec = 35,
                    star2TimeSec = 60
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1800,
                targetScore2Star = 3500,
                targetScore3Star = 5500,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 40,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Mastery: Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 3 (Global 12)
        StageDefinition(
            stageId = StageId(2, 3),
            blueprint = StageBlueprint(
                stageName = "TWIN CRUCIBLE CONVERGENCE",
                directive = "Neutralize 2 Amber Furnaces located on opposing diagonals.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 CRUCIBLES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 40,
                    star2TimeSec = 70
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 5, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2000,
                targetScore2Star = 4000,
                targetScore3Star = 6000,
                moveBudgetStar2 = 15,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Mastery: Detonate both furnaces with 0 slag created"
                )
            )
        ),
        // Stage 4 (Global 13)
        StageDefinition(
            stageId = StageId(2, 4),
            blueprint = StageBlueprint(
                stageName = "SOLAR CONDUIT SYNTHESIS",
                directive = "Synthesize and clear 35 Circuit Conduit tiles.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "35 CIRCUIT TILES",
                    targetAmount = 35,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2200,
                targetScore2Star = 4500,
                targetScore3Star = 7000,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 4500,
                    description = "Mastery: Accumulate ≥ 4,500 points"
                )
            )
        ),
        // Stage 5 (Global 14)
        StageDefinition(
            stageId = StageId(2, 5),
            blueprint = StageBlueprint(
                stageName = "TRI-APERTURE MELTDOWN",
                directive = "Neutralize 3 Amber Furnaces before they transmute into slag.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CRUCIBLES PURGED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 90
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 4500,
                targetScore3Star = 6500,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Mastery: Execute a 2+ line clear in a single drop"
                )
            )
        ),
        // Stage 6 (Global 15)
        StageDefinition(
            stageId = StageId(2, 6),
            blueprint = StageBlueprint(
                stageName = "THERMAL MOMENTUM HARMONIC",
                directive = "Achieve a 4x Surge Streak before any furnace expires.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "4x SURGE STREAK",
                    targetAmount = 4,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2800,
                targetScore2Star = 5200,
                targetScore3Star = 8000,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 4,
                    description = "Mastery: Reach a 4x Surge Streak"
                )
            )
        ),
        // Stage 7 (Global 16)
        StageDefinition(
            stageId = StageId(2, 7),
            blueprint = StageBlueprint(
                stageName = "QUAD CRUCIBLE OVERLOAD",
                directive = "Neutralize all 4 corner Amber Furnaces using cross-laser chain reactions.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CRUCIBLES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 55,
                    star2TimeSec = 100
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 5000,
                targetScore3Star = 7500,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 70,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 4500,
                    description = "Mastery: Accumulate ≥ 4,500 points"
                )
            )
        ),
        // Stage 8 (Global 17)
        StageDefinition(
            stageId = StageId(2, 8),
            blueprint = StageBlueprint(
                stageName = "MOLTEN REINFORCEMENTS",
                directive = "Neutralize 4 Amber Furnaces across 2 consecutive thermal waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CRUCIBLES (2 WAVES)",
                    targetAmount = 4,
                    star3TimeSec = 60,
                    star2TimeSec = 110
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3200,
                targetScore2Star = 6500,
                targetScore3Star = 9500,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 80,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Mastery: Chain a 3x Streak through Wave 2"
                )
            )
        ),
        // Stage 9 (Global 18, Boss)
        StageDefinition(
            stageId = StageId(2, 9),
            blueprint = StageBlueprint(
                stageName = "SOLAR COLOSSUS // APEX",
                directive = "Phase 1: Destroy 4 Thermal Relay Pylons.\nPhase 2: Strike the central Solar Core before it vents heat slag.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "SOLAR COLOSSUS DESTROYED",
                    targetAmount = 5,
                    star3TimeSec = 85,
                    star2TimeSec = 150
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4000,
                targetScore2Star = 7500,
                targetScore3Star = 11000,
                moveBudgetStar2 = 28,
                timeLimitSecStar2 = 100,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Mastery: Defeat Solar Colossus with 0 slots jammed"
                )
            )
        )
    )
)
