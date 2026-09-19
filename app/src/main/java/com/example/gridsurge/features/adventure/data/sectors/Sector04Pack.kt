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
        // Stage 1: Incursion Calibration (Off-Center Single Conduit)
        StageDefinition(
            stageId = StageId(4, 1),
            blueprint = StageBlueprint(
                stageName = "BIO-CONDUIT INITIATION",
                directive = "Purge the off-center Bio-Conduit before it secretes toxic slime into adjacent cells.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 BIO-CONDUIT PURGED",
                    targetAmount = 1,
                    star3TimeSec = 35,
                    star2TimeSec = 65
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 5, row = 2, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1500,
                targetScore2Star = 2500,
                targetScore3Star = 3500,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines before slime spreads"
                )
            )
        ),
        // Stage 2: Line Cleanse Protocol
        StageDefinition(
            stageId = StageId(4, 2),
            blueprint = StageBlueprint(
                stageName = "CONTAGION CONTAINMENT",
                directive = "Complete 7 full line clears to sterilize infected grid channels.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.LINE_CLEANSE,
                    title = "7 LINES CLEARED",
                    targetAmount = 7,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2000,
                targetScore2Star = 4000,
                targetScore3Star = 6000,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak"
                )
            )
        ),
        // Stage 3: Asymmetrical Duo
        StageDefinition(
            stageId = StageId(4, 3),
            blueprint = StageBlueprint(
                stageName = "SLIME DISSOLUTION",
                directive = "Purge 2 Bio-Conduits in an offset configuration before bio-hazard leakage.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 BIO-CONDUITS PURGED",
                    targetAmount = 2,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 4, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 4, row = 2, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 4800,
                targetScore3Star = 7500,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 4: Chroma Synthesis
        StageDefinition(
            stageId = StageId(4, 4),
            blueprint = StageBlueprint(
                stageName = "BIO-SYNTHESIS ARRAY",
                directive = "Synthesize 40 energy tiles while neutralizing contagion pockets.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "40 TILES SYNTHESIZED",
                    targetAmount = 40,
                    star3TimeSec = 50,
                    star2TimeSec = 90
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 5800,
                targetScore3Star = 8800,
                moveBudgetStar2 = 20,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 6500,
                    description = "Reach 6,500 points during synthesis"
                )
            )
        ),
        // Stage 5: Perimeter Confinement
        StageDefinition(
            stageId = StageId(4, 5),
            blueprint = StageBlueprint(
                stageName = "CONDUIT WALL LOCKDOWN",
                directive = "Purge 3 Bio-Conduits aligned along the southern perimeter.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CONDUITS PURGED",
                    targetAmount = 3,
                    star3TimeSec = 55,
                    star2TimeSec = 100
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 5, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 4, row = 5, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3200,
                targetScore2Star = 6500,
                targetScore3Star = 10000,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 75,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 6: Momentum Harmonic
        StageDefinition(
            stageId = StageId(4, 6),
            blueprint = StageBlueprint(
                stageName = "VIRAL STREAK HARMONIC",
                directive = "Sustain a 4x Surge Streak to disintegrate the toxic conduit core.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "4X SURGE STREAK REACHED",
                    targetAmount = 4,
                    star3TimeSec = 55,
                    star2TimeSec = 105
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 4, row = 4, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 4)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 7200,
                targetScore3Star = 11200,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 80,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 4,
                    description = "Reach a 4x Surge Streak"
                )
            )
        ),
        // Stage 7: Subspace Overclock
        StageDefinition(
            stageId = StageId(4, 7),
            blueprint = StageBlueprint(
                stageName = "QUAD CONDUIT CHOKEPOINT",
                directive = "Purge 4 Bio-Conduits at asymmetric choke points before toxic flooding.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CONDUITS PURGED",
                    targetAmount = 4,
                    star3TimeSec = 60,
                    star2TimeSec = 110
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 0, row = 3, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 3, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 4, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 7, row = 4, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4000,
                targetScore2Star = 8200,
                targetScore3Star = 12500,
                moveBudgetStar2 = 28,
                timeLimitSecStar2 = 90,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 8: Dual-Wave Escalation
        StageDefinition(
            stageId = StageId(4, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE BIO ESCALATION",
                directive = "Purge 5 Bio-Conduits across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "5 CONDUITS PURGED (2 WAVES)",
                    targetAmount = 5,
                    star3TimeSec = 70,
                    star2TimeSec = 120
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 2)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4500,
                targetScore2Star = 9000,
                targetScore3Star = 13800,
                moveBudgetStar2 = 30,
                timeLimitSecStar2 = 100,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 9: Sector Apex Boss
        StageDefinition(
            stageId = StageId(4, 9),
            blueprint = StageBlueprint(
                stageName = "BIO-TITAN OVERLORD // APEX",
                directive = "Phase 1: Destroy 4 Contagion Pylons.\nPhase 2: Strike the central Bio-Titan Core 4x to purge contagion.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "BIO-TITAN DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 90,
                    star2TimeSec = 140
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 4, isLocked = true),
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 1),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 1),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.BIO_CONDUIT_SEC4, maxHits = 1)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5500,
                targetScore2Star = 10000,
                targetScore3Star = 15000,
                moveBudgetStar2 = 32,
                timeLimitSecStar2 = 110,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Bio-Titan with 0 slots jammed"
                )
            )
        )
    )
)
