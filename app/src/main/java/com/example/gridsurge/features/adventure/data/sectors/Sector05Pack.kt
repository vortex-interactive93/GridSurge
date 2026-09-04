package com.example.gridsurge.features.adventure.data.sectors

import com.example.gridsurge.features.adventure.domain.model.*
import com.example.gridsurge.features.adventure.domain.provider.BaseSectorPack
import com.example.gridsurge.features.adventure.model.*

object Sector05Pack : BaseSectorPack(
    metadata = SectorMetadata(
        sectorId = 5,
        sectorName = "QUANTUM SINGULARITY",
        subtitle = "EVENT HORIZON WARP",
        visualThemeKey = "THEME_QUANTUM_SINGULARITY",
        totalStages = 9,
        rewardTitle = "EVENT HORIZON ELITE",
        rewardStarGrant = 20,
        baseDifficultyRating = 5
    ),
    stages = listOf(
        // Stage 1 (Global Stage 37) - Calibration Stage
        StageDefinition(
            stageId = StageId(5, 1),
            blueprint = StageBlueprint(
                stageName = "GRAVITATION INITIATION",
                directive = "Purge the central Event Horizon Gyro before its gravitational pull warps surrounding blocks.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 VOID CORE PURGED",
                    targetAmount = 1,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 6000,
                targetScore3Star = 9000,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines before gravitational shift"
                )
            )
        ),
        // Stage 2
        StageDefinition(
            stageId = StageId(5, 2),
            blueprint = StageBlueprint(
                stageName = "DUAL HORIZON CONVERGENCE",
                directive = "Neutralize 2 Void Cores on opposing diagonals amidst spatial warping.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 VOID CORES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 45,
                    star2TimeSec = 85
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 5, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 7000,
                targetScore3Star = 10500,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 3
        StageDefinition(
            stageId = StageId(5, 3),
            blueprint = StageBlueprint(
                stageName = "TRI-GYRO SINGULARITY",
                directive = "Purge 3 Void Cores forming the spatial containment triangle.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 VOID CORES PURGED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4000,
                targetScore2Star = 8000,
                targetScore3Star = 12000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 7500,
                    description = "Accumulate ≥ 7,500 points"
                )
            )
        ),
        // Stage 4
        StageDefinition(
            stageId = StageId(5, 4),
            blueprint = StageBlueprint(
                stageName = "QUANTUM CHROMA WARP",
                directive = "Synthesize 35 Circuit Conduit tiles amidst gravitational distortions.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "35 CIRCUIT TILES",
                    targetAmount = 35,
                    star3TimeSec = 60,
                    star2TimeSec = 110
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 4, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4500,
                targetScore2Star = 8500,
                targetScore3Star = 13000,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 75,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 3,
                    description = "Trigger a 3-line Mega Blitz clear"
                )
            )
        ),
        // Stage 5
        StageDefinition(
            stageId = StageId(5, 5),
            blueprint = StageBlueprint(
                stageName = "QUAD HORIZON WARP",
                directive = "Purge all 4 corner Void Cores using Supernova Overdrive and combo chains.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 VOID CORES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 60,
                    star2TimeSec = 115
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5000,
                targetScore2Star = 9000,
                targetScore3Star = 14000,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 80,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Reach a 3x Surge Streak"
                )
            )
        ),
        // Stage 6
        StageDefinition(
            stageId = StageId(5, 6),
            blueprint = StageBlueprint(
                stageName = "EVENT HORIZON HARMONIC",
                directive = "Achieve a 5x Surge Streak in maximum gravitational instability.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "5x SURGE STREAK",
                    targetAmount = 5,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5200,
                targetScore2Star = 9500,
                targetScore3Star = 15000,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 4,
                    description = "Reach a 4x Surge Streak"
                )
            )
        ),
        // Stage 7
        StageDefinition(
            stageId = StageId(5, 7),
            blueprint = StageBlueprint(
                stageName = "SINGULARITY OVERCLOCK",
                directive = "Purge 4 corner Void Cores under extreme time constraints.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 VOID CORES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 65,
                    star2TimeSec = 125
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5500,
                targetScore2Star = 10000,
                targetScore3Star = 16000,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 90,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 10000,
                    description = "Accumulate ≥ 10,000 points"
                )
            )
        ),
        // Stage 8
        StageDefinition(
            stageId = StageId(5, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE HORIZON WARP",
                directive = "Purge 4 Void Cores across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CORES (2 WAVES)",
                    targetAmount = 4,
                    star3TimeSec = 75,
                    star2TimeSec = 140
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 6000,
                targetScore2Star = 11000,
                targetScore3Star = 17500,
                moveBudgetStar2 = 28,
                timeLimitSecStar2 = 100,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Chain a 3x Streak through Wave 2"
                )
            )
        ),
        // Stage 9 (Final Campaign Apex Boss)
        StageDefinition(
            stageId = StageId(5, 9),
            blueprint = StageBlueprint(
                stageName = "EVENT HORIZON // FINAL APEX",
                directive = "Phase 1: Destroy 4 Gravitational Relay Pylons.\nPhase 2: Strike the central Event Horizon Core to collapse the void matrix forever.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "EVENT HORIZON DESTROYED",
                    targetAmount = 5,
                    star3TimeSec = 100,
                    star2TimeSec = 180
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 7000,
                targetScore2Star = 13000,
                targetScore3Star = 20000,
                moveBudgetStar2 = 35,
                timeLimitSecStar2 = 130,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Final Apex Boss with 0 slots jammed"
                )
            )
        )
    )
)
