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
        // Stage 1: Incursion Calibration (Off-Center Single Core)
        StageDefinition(
            stageId = StageId(1, 1),
            blueprint = StageBlueprint(
                stageName = "CHRONO CALIBRATION",
                directive = "Crack and detonate the off-center Chrono Reactor.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 CHRONO REACTOR PURGED",
                    targetAmount = 1,
                    star3TimeSec = 25,
                    star2TimeSec = 45
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 800,
                targetScore2Star = 1200,
                targetScore3Star = 1500,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 35,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines in a single drop"
                )
            )
        ),
        // Stage 2: Line Cleanse Protocol
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
                targetScore1Star = 1000,
                targetScore2Star = 1800,
                targetScore3Star = 2500,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 40,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 3: Asymmetrical Duo
        StageDefinition(
            stageId = StageId(1, 3),
            blueprint = StageBlueprint(
                stageName = "OFFSET HARMONICS",
                directive = "Neutralize 2 Chrono Reactors in an asymmetrical offset layout.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 CORES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 35,
                    star2TimeSec = 65
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1200,
                targetScore2Star = 2500,
                targetScore3Star = 3800,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 4: Chroma Synthesis
        StageDefinition(
            stageId = StageId(1, 4),
            blueprint = StageBlueprint(
                stageName = "SYNTHESIS ARRAY",
                directive = "Synthesize 25 energy tiles through active block placement.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "25 TILES SYNTHESIZED",
                    targetAmount = 25,
                    star3TimeSec = 40,
                    star2TimeSec = 70
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1500,
                targetScore2Star = 3000,
                targetScore3Star = 4500,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 3500,
                    description = "Reach 3,500 points during synthesis"
                )
            )
        ),
        // Stage 5: Perimeter Confinement
        StageDefinition(
            stageId = StageId(1, 5),
            blueprint = StageBlueprint(
                stageName = "PERIMETER LOCKDOWN",
                directive = "Purge 3 Chrono Reactors clustered along the western perimeter.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CORES PURGED",
                    targetAmount = 3,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 1, row = 4, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1800,
                targetScore2Star = 3500,
                targetScore3Star = 5200,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 6: Momentum Harmonic
        StageDefinition(
            stageId = StageId(1, 6),
            blueprint = StageBlueprint(
                stageName = "MOMENTUM HARMONIC",
                directive = "Sustain a 3x Surge Streak to overcharge the central reactor.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "3X SURGE STREAK REACHED",
                    targetAmount = 3,
                    star3TimeSec = 45,
                    star2TimeSec = 85
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 3)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2000,
                targetScore2Star = 3800,
                targetScore3Star = 6000,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Reach a 3x Surge Streak"
                )
            )
        ),
        // Stage 7: Subspace Overclock
        StageDefinition(
            stageId = StageId(1, 7),
            blueprint = StageBlueprint(
                stageName = "SUBSPACE OVERCLOCK",
                directive = "Neutralize 4 Chrono Reactors situated at asymmetric choke points.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CORES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 0, row = 2, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 2, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 7, row = 5, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2200,
                targetScore2Star = 4200,
                targetScore3Star = 6800,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 75,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 8: Dual-Wave Escalation
        StageDefinition(
            stageId = StageId(1, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE ESCALATION",
                directive = "Purge 4 Chrono Reactors across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CORES PURGED (2 WAVES)",
                    targetAmount = 4,
                    star3TimeSec = 55,
                    star2TimeSec = 105
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 5000,
                targetScore3Star = 7200,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 85,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 9: Sector Apex Boss
        StageDefinition(
            stageId = StageId(1, 9),
            blueprint = StageBlueprint(
                stageName = "NEON GUARDIAN // APEX",
                directive = "Phase 1: Destroy 4 Offset Pylons.\nPhase 2: Strike the central Guardian Core 3x to shatter the matrix.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "NEON GUARDIAN DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 75,
                    star2TimeSec = 120
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.CHRONO_REACTOR_SEC1, maxHits = 1)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 5500,
                targetScore3Star = 8000,
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
