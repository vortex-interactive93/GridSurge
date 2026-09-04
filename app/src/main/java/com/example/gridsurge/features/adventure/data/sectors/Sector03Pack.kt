package com.example.gridsurge.features.adventure.data.sectors

import com.example.gridsurge.features.adventure.domain.model.*
import com.example.gridsurge.features.adventure.domain.provider.BaseSectorPack
import com.example.gridsurge.features.adventure.model.*

object Sector03Pack : BaseSectorPack(
    metadata = SectorMetadata(
        sectorId = 3,
        sectorName = "CRIMSON BREACH",
        subtitle = "CIPHER LOCKDOWN",
        visualThemeKey = "THEME_CRIMSON_BREACH",
        totalStages = 9,
        rewardTitle = "BREACH OPERATIVE",
        rewardStarGrant = 12,
        baseDifficultyRating = 3
    ),
    stages = listOf(
        // Stage 1 (Global Stage 19) - Calibration Stage
        StageDefinition(
            stageId = StageId(3, 1),
            blueprint = StageBlueprint(
                stageName = "CIPHER CALIBRATION",
                directive = "Sustain a 2x Surge Streak to shatter the Cipher lock and purge the core.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 CRIMSON CIPHER PURGED",
                    targetAmount = 1,
                    star3TimeSec = 35,
                    star2TimeSec = 65
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2000,
                targetScore2Star = 4000,
                targetScore3Star = 6500,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak to unlock Cipher"
                )
            )
        ),
        // Stage 2
        StageDefinition(
            stageId = StageId(3, 2),
            blueprint = StageBlueprint(
                stageName = "SURGE DECRYPTION",
                directive = "Unlock and purge 2 Crimson Ciphers on opposing diagonals.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 CRIMSON CIPHERS PURGED",
                    targetAmount = 2,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 5, row = 5, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 4800,
                targetScore3Star = 7500,
                moveBudgetStar2 = 14,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Clear 2+ lines in a single drop"
                )
            )
        ),
        // Stage 3
        StageDefinition(
            stageId = StageId(3, 3),
            blueprint = StageBlueprint(
                stageName = "DUAL CIPHER REINFORCEMENTS",
                directive = "Purge 3 Crimson Ciphers while maintaining grid stability.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CIPHERS PURGED",
                    targetAmount = 3,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3000,
                targetScore2Star = 5500,
                targetScore3Star = 8500,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 55,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Reach a 3x Surge Streak"
                )
            )
        ),
        // Stage 4
        StageDefinition(
            stageId = StageId(3, 4),
            blueprint = StageBlueprint(
                stageName = "CHROMA CIPHER SYNTHESIS",
                directive = "Synthesize and clear 25 Circuit Conduit tiles amidst locked ciphers.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "25 CIRCUIT TILES",
                    targetAmount = 25,
                    star3TimeSec = 50,
                    star2TimeSec = 90
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3200,
                targetScore2Star = 6000,
                targetScore3Star = 9000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 5000,
                    description = "Accumulate ≥ 5,000 points"
                )
            )
        ),
        // Stage 5
        StageDefinition(
            stageId = StageId(3, 5),
            blueprint = StageBlueprint(
                stageName = "TRI-CIPHER MATRIX",
                directive = "Shatter locks on 3 perimeter Crimson Ciphers and detonate them.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CIPHERS PURGED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3500,
                targetScore2Star = 6500,
                targetScore3Star = 10000,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 65,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 3,
                    description = "Trigger a 3-line Mega Blitz clear"
                )
            )
        ),
        // Stage 6
        StageDefinition(
            stageId = StageId(3, 6),
            blueprint = StageBlueprint(
                stageName = "MOMENTUM DECRYPTION",
                directive = "Achieve a 4x Surge Streak while unlocking Crimson Ciphers.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "4x SURGE STREAK",
                    targetAmount = 4,
                    star3TimeSec = 45,
                    star2TimeSec = 80
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 5, row = 5, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3800,
                targetScore2Star = 7000,
                targetScore3Star = 10500,
                moveBudgetStar2 = 16,
                timeLimitSecStar2 = 50,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 4,
                    description = "Reach a 4x Surge Streak"
                )
            )
        ),
        // Stage 7
        StageDefinition(
            stageId = StageId(3, 7),
            blueprint = StageBlueprint(
                stageName = "QUAD CIPHER OVERCLOCK",
                directive = "Purge all 4 corner Crimson Ciphers using Solar Cross Laser and combo chains.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CIPHERS PURGED",
                    targetAmount = 4,
                    star3TimeSec = 60,
                    star2TimeSec = 110
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4000,
                targetScore2Star = 7500,
                targetScore3Star = 11500,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 75,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 7000,
                    description = "Accumulate ≥ 7,000 points"
                )
            )
        ),
        // Stage 8
        StageDefinition(
            stageId = StageId(3, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE CIPHER REINFORCEMENTS",
                directive = "Purge 4 Crimson Ciphers across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CIPHERS (2 WAVES)",
                    targetAmount = 4,
                    star3TimeSec = 65,
                    star2TimeSec = 120
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 4200,
                targetScore2Star = 8000,
                targetScore3Star = 12000,
                moveBudgetStar2 = 24,
                timeLimitSecStar2 = 85,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Chain a 3x Streak through Wave 2"
                )
            )
        ),
        // Stage 9 (Boss)
        StageDefinition(
            stageId = StageId(3, 9),
            blueprint = StageBlueprint(
                stageName = "CRIMSON APEX // GUARDIAN",
                directive = "Phase 1: Unlock and destroy 4 Cipher Shield Pylons.\nPhase 2: Strike the central Apex Core before it locks the matrix.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "CRIMSON APEX DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 90,
                    star2TimeSec = 160
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5000,
                targetScore2Star = 9000,
                targetScore3Star = 13500,
                moveBudgetStar2 = 30,
                timeLimitSecStar2 = 110,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Crimson Apex with 0 slots jammed"
                )
            )
        )
    )
)
