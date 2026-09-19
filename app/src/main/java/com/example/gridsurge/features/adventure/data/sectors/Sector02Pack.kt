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
        // Stage 1: Incursion Calibration (Off-Center Single Furnace)
        StageDefinition(
            stageId = StageId(2, 1),
            blueprint = StageBlueprint(
                stageName = "THERMAL IGNITION",
                directive = "Detonate the off-center Solar Crucible before thermal countdown expires.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 SOLAR CRUCIBLE PURGED",
                    targetAmount = 1,
                    star3TimeSec = 30,
                    star2TimeSec = 55
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 4, row = 2, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1000,
                targetScore2Star = 1500,
                targetScore3Star = 2000,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 40,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines before thermal melt"
                )
            )
        ),
        // Stage 2: Line Cleanse Protocol
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
                    star2TimeSec = 65
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1500,
                targetScore2Star = 2800,
                targetScore3Star = 4000,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 3: Asymmetrical Duo
        StageDefinition(
            stageId = StageId(2, 3),
            blueprint = StageBlueprint(
                stageName = "OFFSET CRUCIBLES",
                directive = "Detonate 2 Solar Crucibles at offset coordinates before meltdown.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 CRUCIBLES PURGED",
                    targetAmount = 2,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 4, row = 5, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1800,
                targetScore2Star = 3500,
                targetScore3Star = 5200,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines in a single drop"
                )
            )
        ),
        // Stage 4: Chroma Synthesis
        StageDefinition(
            stageId = StageId(2, 4),
            blueprint = StageBlueprint(
                stageName = "SOLAR SYNTHESIS",
                directive = "Synthesize 30 energy tiles while containing thermal heat.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "30 TILES SYNTHESIZED",
                    targetAmount = 30,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2000,
                targetScore2Star = 4000,
                targetScore3Star = 6000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 4500,
                    description = "Reach 4,500 points during synthesis"
                )
            )
        ),
        // Stage 5: Perimeter Confinement
        StageDefinition(
            stageId = StageId(2, 5),
            blueprint = StageBlueprint(
                stageName = "FURNACE WALL LOCKDOWN",
                directive = "Purge 3 Solar Crucibles positioned in an L-shaped thermal wall.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CRUCIBLES PURGED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 90
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 2, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 4800,
                targetScore3Star = 7200,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 6: Momentum Harmonic
        StageDefinition(
            stageId = StageId(2, 6),
            blueprint = StageBlueprint(
                stageName = "THERMAL STREAK HARMONIC",
                directive = "Sustain a 3x Surge Streak to prevent molten slag explosion.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "3X SURGE STREAK REACHED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 4, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 3)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2800,
                targetScore2Star = 5200,
                targetScore3Star = 8000,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 70,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Reach a 3x Surge Streak"
                )
            )
        ),
        // Stage 7: Subspace Overclock
        StageDefinition(
            stageId = StageId(2, 7),
            blueprint = StageBlueprint(
                stageName = "QUAD CRUCIBLE CHOKEPOINT",
                directive = "Neutralize 4 Solar Crucibles at asymmetric choke points before meltdown.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CRUCIBLES PURGED",
                    targetAmount = 4,
                    star3TimeSec = 55,
                    star2TimeSec = 100
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 4, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 5800,
                targetScore3Star = 9000,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 80,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 8: Dual-Wave Escalation
        StageDefinition(
            stageId = StageId(2, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE CRUCIBLE ESCALATION",
                directive = "Purge 4 Solar Crucibles across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CRUCIBLES PURGED (2 WAVES)",
                    targetAmount = 4,
                    star3TimeSec = 60,
                    star2TimeSec = 110
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 2, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 6500,
                targetScore3Star = 9800,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 90,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 9: Sector Apex Boss
        StageDefinition(
            stageId = StageId(2, 9),
            blueprint = StageBlueprint(
                stageName = "SOLAR FORGE OVERLORD // APEX",
                directive = "Phase 1: Neutralize 4 Thermal Pylons.\nPhase 2: Strike the central Forge Core 3x to extinguish meltdown.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "FORGE OVERLORD DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 80,
                    star2TimeSec = 130
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 1),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.SOLAR_CRUCIBLE_SEC2, maxHits = 1)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4500,
                targetScore2Star = 7500,
                targetScore3Star = 11000,
                moveBudgetStar2 = 28,
                timeLimitSecStar2 = 95,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Overlord with 0 slots jammed"
                )
            )
        )
    )
)
