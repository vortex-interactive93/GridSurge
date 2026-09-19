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
        // Stage 1: Incursion Calibration (Off-Center Single Void Core)
        StageDefinition(
            stageId = StageId(5, 1),
            blueprint = StageBlueprint(
                stageName = "GRAVITATION INITIATION",
                directive = "Purge the off-center Event Horizon Gyro before its gravitational pull warps surrounding blocks.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 VOID CORE PURGED",
                    targetAmount = 1,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 2, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1800,
                targetScore2Star = 3000,
                targetScore3Star = 4200,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines before gravitational shift"
                )
            )
        ),
        // Stage 2: Line Cleanse Protocol
        StageDefinition(
            stageId = StageId(5, 2),
            blueprint = StageBlueprint(
                stageName = "SINGULARITY SWEEP",
                directive = "Complete 8 full line clears amidst active spatial distorting fields.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.LINE_CLEANSE,
                    title = "8 LINES CLEARED",
                    targetAmount = 8,
                    star3TimeSec = 45,
                    star2TimeSec = 85
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 5000,
                targetScore3Star = 7500,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 3: Asymmetrical Hybrid Duo (Void Core + Locked Crimson Cipher)
        StageDefinition(
            stageId = StageId(5, 3),
            blueprint = StageBlueprint(
                stageName = "HYBRID SINGULARITY",
                directive = "Sustain a Surge Streak to unlock the Crimson Cipher while purging the adjacent Void Core.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 HYBRID CORES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 50,
                    star2TimeSec = 90
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 5, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 2, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 6000,
                targetScore3Star = 9000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 4: Chroma Synthesis
        StageDefinition(
            stageId = StageId(5, 4),
            blueprint = StageBlueprint(
                stageName = "EVENT HORIZON SYNTHESIS",
                directive = "Synthesize 45 energy tiles amidst event horizon gravitational distortion.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "45 TILES SYNTHESIZED",
                    targetAmount = 45,
                    star3TimeSec = 55,
                    star2TimeSec = 100
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 7000,
                targetScore3Star = 11000,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 70,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 8000,
                    description = "Reach 8,000 points during synthesis"
                )
            )
        ),
        // Stage 5: Perimeter Confinement (Hybrid: 2 Void Cores + 1 Solar Furnace)
        StageDefinition(
            stageId = StageId(5, 5),
            blueprint = StageBlueprint(
                stageName = "HYBRID CONFINEMENT WALL",
                directive = "Purge 2 Void Cores and 1 countdown Solar Furnace in a tight perimeter wall.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 HYBRID CORES PURGED",
                    targetAmount = 3,
                    star3TimeSec = 60,
                    star2TimeSec = 110
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4000,
                targetScore2Star = 8000,
                targetScore3Star = 12500,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 80,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 6: Momentum Harmonic
        StageDefinition(
            stageId = StageId(5, 6),
            blueprint = StageBlueprint(
                stageName = "QUANTUM STREAK HARMONIC",
                directive = "Sustain a 5x Surge Streak to collapse the Singularity Core.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "5X SURGE STREAK REACHED",
                    targetAmount = 5,
                    star3TimeSec = 65,
                    star2TimeSec = 115
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 4)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4500,
                targetScore2Star = 9000,
                targetScore3Star = 14000,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 85,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 5,
                    description = "Reach a 5x Surge Streak"
                )
            )
        ),
        // Stage 7: Subspace Overclock (4 Asymmetric Hybrid Chokepoints)
        StageDefinition(
            stageId = StageId(5, 7),
            blueprint = StageBlueprint(
                stageName = "HYBRID QUAD CHOKEPOINT",
                directive = "Purge 4 hybrid cores (Void, Furnace, Cipher) placed at asymmetric choke points.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 HYBRID CORES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 70,
                    star2TimeSec = 120
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 2, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5000,
                targetScore2Star = 10000,
                targetScore3Star = 15500,
                moveBudgetStar2 = 30,
                timeLimitSecStar2 = 95,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 8: Dual-Wave Escalation
        StageDefinition(
            stageId = StageId(5, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE QUANTUM ESCALATION",
                directive = "Purge 5 hybrid cores across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "5 HYBRID CORES PURGED (2 WAVES)",
                    targetAmount = 5,
                    star3TimeSec = 80,
                    star2TimeSec = 130
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5500,
                targetScore2Star = 11000,
                targetScore3Star = 16800,
                moveBudgetStar2 = 32,
                timeLimitSecStar2 = 105,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 9: Sector Apex Boss
        StageDefinition(
            stageId = StageId(5, 9),
            blueprint = StageBlueprint(
                stageName = "QUANTUM SINGULARITY OVERLORD // APEX",
                directive = "Phase 1: Neutralize 4 Hybrid Pylons (Cipher/Furnace/Void).\nPhase 2: Shatter the central Singularity Core 4x.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "SINGULARITY OVERLORD DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 95,
                    star2TimeSec = 150
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 4, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 1, isLocked = true),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 1),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.VOID_SINGULARITY_SEC5, maxHits = 1)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 6500,
                targetScore2Star = 12000,
                targetScore3Star = 18000,
                moveBudgetStar2 = 35,
                timeLimitSecStar2 = 120,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Apex Singularity with 0 slots jammed"
                )
            )
        )
    )
)
