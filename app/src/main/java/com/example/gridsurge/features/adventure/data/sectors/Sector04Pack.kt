package com.example.gridsurge.features.adventure.data.sectors

import com.example.gridsurge.features.adventure.domain.model.*
import com.example.gridsurge.features.adventure.domain.provider.BaseSectorPack
import com.example.gridsurge.features.adventure.model.*

object Sector04Pack : BaseSectorPack(
    metadata = SectorMetadata(
        sectorId = 4,
        sectorName = "TOXIC SURGE",
        subtitle = "BIO-HAZARD CONTAGION",
        visualThemeKey = "THEME_TOXIC_SURGE",
        totalStages = 9,
        rewardTitle = "GRID OVERLORD",
        rewardStarGrant = 15,
        baseDifficultyRating = 4
    ),
    stages = listOf(
        // Stage 1 (Global Stage 28) - Calibration Stage
        StageDefinition(
            stageId = StageId(4, 1),
            blueprint = StageBlueprint(
                stageName = "BIO-CONDUIT INITIATION",
                directive = "Purge the central Bio-Conduit before it secretes toxic slime into adjacent cells.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 BIO-CONDUIT PURGED",
                    targetAmount = 1,
                    star3TimeSec = 35,
                    star2TimeSec = 65
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 4800,
                targetScore3Star = 7500,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines before slime spreads"
                )
            )
        ),
        // Stage 2
        StageDefinition(
            stageId = StageId(4, 2),
            blueprint = StageBlueprint(
                stageName = "CONTAGION CONTAINMENT",
                directive = "Purge 2 Bio-Conduits on opposing diagonals before bio-hazard leakage.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 BIO-CONDUITS PURGED",
                    targetAmount = 2,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 5, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 5500,
                targetScore3Star = 8500,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 3
        StageDefinition(
            stageId = StageId(4, 3),
            blueprint = StageBlueprint(
                stageName = "TRI-CONDUIT OUTBREAK",
                directive = "Neutralize 3 Bio-Conduits forming the outer containment perimeter.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CONDUITS PURGED",
                    targetAmount = 3,
                    star3TimeSec = 45,
                    star2TimeSec = 85
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 6500,
                targetScore3Star = 9500,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 6000,
                    description = "Accumulate ≥ 6,000 points"
                )
            )
        ),
        // Stage 4
        StageDefinition(
            stageId = StageId(4, 4),
            blueprint = StageBlueprint(
                stageName = "TOXIC CHROMA SYNTHESIS",
                directive = "Synthesize 30 Circuit Conduit tiles while containing bio-hazard leaks.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "30 CIRCUIT TILES",
                    targetAmount = 30,
                    star3TimeSec = 55,
                    star2TimeSec = 100
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 4, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3800,
                targetScore2Star = 7000,
                targetScore3Star = 10500,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 3,
                    description = "Trigger a 3-line Mega Blitz clear"
                )
            )
        ),
        // Stage 5
        StageDefinition(
            stageId = StageId(4, 5),
            blueprint = StageBlueprint(
                stageName = "QUAD CONDUIT SPILLWAY",
                directive = "Neutralize 4 Bio-Conduits across the perimeter.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CONDUITS PURGED",
                    targetAmount = 4,
                    star3TimeSec = 55,
                    star2TimeSec = 105
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4000,
                targetScore2Star = 7500,
                targetScore3Star = 11000,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 70,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Reach a 3x Surge Streak"
                )
            )
        ),
        // Stage 6
        StageDefinition(
            stageId = StageId(4, 6),
            blueprint = StageBlueprint(
                stageName = "BIO-SURGE MOMENTUM",
                directive = "Achieve a 4x Surge Streak before bio-slimes overcrowd the matrix.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "4x SURGE STREAK",
                    targetAmount = 4,
                    star3TimeSec = 45,
                    star2TimeSec = 85
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4200,
                targetScore2Star = 8000,
                targetScore3Star = 12000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 4,
                    description = "Reach a 4x Surge Streak"
                )
            )
        ),
        // Stage 7
        StageDefinition(
            stageId = StageId(4, 7),
            blueprint = StageBlueprint(
                stageName = "TOXIC OVERCLOCK",
                directive = "Purge 4 corner Bio-Conduits using Warp Injector and line cleanses.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CONDUITS PURGED",
                    targetAmount = 4,
                    star3TimeSec = 60,
                    star2TimeSec = 115
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4500,
                targetScore2Star = 8500,
                targetScore3Star = 13000,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 80,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 8000,
                    description = "Accumulate ≥ 8,000 points"
                )
            )
        ),
        // Stage 8
        StageDefinition(
            stageId = StageId(4, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE CONTAGION",
                directive = "Purge 4 Bio-Conduits across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CONDUITS (2 WAVES)",
                    targetAmount = 4,
                    star3TimeSec = 70,
                    star2TimeSec = 130
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4800,
                targetScore2Star = 9000,
                targetScore3Star = 14000,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 90,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Chain a 3x Streak through Wave 2"
                )
            )
        ),
        // Stage 9 (Boss)
        StageDefinition(
            stageId = StageId(4, 9),
            blueprint = StageBlueprint(
                stageName = "BIO-COLOSSUS // APEX",
                directive = "Phase 1: Destroy 4 Bio-Relay Pylons.\nPhase 2: Strike the central Bio-Core before toxic sludge covers the grid.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "BIO-COLOSSUS DESTROYED",
                    targetAmount = 5,
                    star3TimeSec = 95,
                    star2TimeSec = 170
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5500,
                targetScore2Star = 10000,
                targetScore3Star = 15000,
                moveBudgetStar2 = 32,
                timeLimitSecStar2 = 120,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Bio-Colossus with 0 slots jammed"
                )
            )
        )
    )
)
