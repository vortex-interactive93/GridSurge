package com.example.gridsurge.features.adventure.data.sectors

import com.example.gridsurge.features.adventure.domain.model.*
import com.example.gridsurge.features.adventure.domain.provider.BaseSectorPack
import com.example.gridsurge.features.adventure.model.*

object Sector01Pack : BaseSectorPack(
    metadata = SectorMetadata(
        sectorId = 1,
        sectorName = "CHRONO NEXUS",
        subtitle = "RESONANCE CASCADE",
        visualThemeKey = "THEME_NEON_NEXUS",
        totalStages = 9,
        rewardTitle = "CHRONO BADGE",
        rewardStarGrant = 5,
        baseDifficultyRating = 1
    ),
    stages = listOf(
        // Stage 1
        StageDefinition(
            stageId = StageId(1, 1),
            blueprint = StageBlueprint(
                stageName = "CHRONO CALIBRATION",
                directive = "Crack and detonate the central Chrono Reactor.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 CHRONO REACTOR PURGED",
                    targetAmount = 1,
                    star3TimeSec = 25,
                    star2TimeSec = 45
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1000,
                targetScore2Star = 2000,
                targetScore3Star = 3500,
                moveBudgetStar2 = 10,
                timeLimitSecStar2 = 35,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines in a single drop"
                )
            )
        ),
        // Stage 2
        StageDefinition(
            stageId = StageId(1, 2),
            blueprint = StageBlueprint(
                stageName = "SURGE RESONANCE",
                directive = "Complete 4 full line clears to calibrate the grid conduit.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.LINE_CLEANSE,
                    title = "4 LINES CLEARED",
                    targetAmount = 4,
                    star3TimeSec = 30,
                    star2TimeSec = 55
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1200,
                targetScore2Star = 2500,
                targetScore3Star = 4000,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 40,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 3
        StageDefinition(
            stageId = StageId(1, 3),
            blueprint = StageBlueprint(
                stageName = "DUAL REACTOR CORE",
                directive = "Neutralize 2 Chrono Reactors located on opposing diagonals.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 CORES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 35,
                    star2TimeSec = 65
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 5, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1500,
                targetScore2Star = 3000,
                targetScore3Star = 4500,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 4
        StageDefinition(
            stageId = StageId(1, 4),
            blueprint = StageBlueprint(
                stageName = "CHROMA EXTRACTION",
                directive = "Synthesize and clear 45 Circuit Conduit tiles.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "45 CIRCUIT TILES",
                    targetAmount = 45,
                    star3TimeSec = 60,
                    star2TimeSec = 100
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1800,
                targetScore2Star = 3200,
                targetScore3Star = 5000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 3500,
                    description = "Accumulate ≥ 3,500 points"
                )
            )
        ),
        // Stage 5
        StageDefinition(
            stageId = StageId(1, 5),
            blueprint = StageBlueprint(
                stageName = "TRI-CONDUIT TRIANGULATION",
                directive = "Neutralize 3 Chrono Reactors forming the perimeter triangle.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CORES PURGED",
                    targetAmount = 3,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2000,
                targetScore2Star = 3500,
                targetScore3Star = 5000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a 2+ line clear in a single drop"
                )
            )
        ),
        // Stage 6
        StageDefinition(
            stageId = StageId(1, 6),
            blueprint = StageBlueprint(
                stageName = "MOMENTUM HARMONIC",
                directive = "Achieve a 5x Surge Streak without exhausting combo grace moves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "5x SURGE STREAK",
                    targetAmount = 5,
                    star3TimeSec = 50,
                    star2TimeSec = 90
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2200,
                targetScore2Star = 3800,
                targetScore3Star = 6000,
                moveBudgetStar2 = 15,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 4,
                    description = "Reach a 4x Surge Streak"
                )
            )
        ),
        // Stage 7
        StageDefinition(
            stageId = StageId(1, 7),
            blueprint = StageBlueprint(
                stageName = "QUAD MATRIX OVERCLOCK",
                directive = "Neutralize all 4 corner Chrono Reactors.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CORES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 3800,
                targetScore3Star = 5000,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 75,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 3200,
                    description = "Accumulate ≥ 3,200 points"
                )
            )
        ),
        // Stage 8
        StageDefinition(
            stageId = StageId(1, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE REINFORCEMENTS",
                directive = "Neutralize 3 Chrono Reactors across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CORES PURGED (2 WAVES)",
                    targetAmount = 3,
                    star3TimeSec = 55,
                    star2TimeSec = 105
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2800,
                targetScore2Star = 4000,
                targetScore3Star = 6000,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 85,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 9 (Boss)
        StageDefinition(
            stageId = StageId(1, 9),
            blueprint = StageBlueprint(
                stageName = "NEON GUARDIAN // APEX",
                directive = "Phase 1: Destroy 4 Shield Pylons.\nPhase 2: Strike the central Guardian Core 3x to shatter the matrix.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "NEON GUARDIAN DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 60,
                    star2TimeSec = 120
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 5500,
                targetScore3Star = 8500,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 90,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Apex Boss with 0 slots jammed"
                )
            )
        )
    )
)
