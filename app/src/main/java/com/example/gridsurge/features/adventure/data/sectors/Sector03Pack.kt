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
        // Stage 1: Incursion Calibration (Off-Center Locked Cipher)
        StageDefinition(
            stageId = StageId(3, 1),
            blueprint = StageBlueprint(
                stageName = "CIPHER CALIBRATION",
                directive = "Sustain a 2x Surge Streak to unlock and purge the off-center Crimson Cipher.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "1 CRIMSON CIPHER PURGED",
                    targetAmount = 1,
                    star3TimeSec = 35,
                    star2TimeSec = 65
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 4, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1200,
                targetScore2Star = 2000,
                targetScore3Star = 2800,
                moveBudgetStar2 = 12,
                timeLimitSecStar2 = 45,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 2,
                    description = "Sustain a 2x Surge Streak to unlock Cipher"
                )
            )
        ),
        // Stage 2: Line Cleanse Protocol
        StageDefinition(
            stageId = StageId(3, 2),
            blueprint = StageBlueprint(
                stageName = "DECRYPTION SWEEP",
                directive = "Complete 6 full line clears to breach encryption locks.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.LINE_CLEANSE,
                    title = "6 LINES CLEARED",
                    targetAmount = 6,
                    star3TimeSec = 40,
                    star2TimeSec = 70
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 1800,
                targetScore2Star = 3200,
                targetScore3Star = 5000,
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
            stageId = StageId(3, 3),
            blueprint = StageBlueprint(
                stageName = "SURGE DECRYPTION",
                directive = "Unlock and purge 2 Crimson Ciphers in an offset layout.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "2 CRIMSON CIPHERS PURGED",
                    targetAmount = 2,
                    star3TimeSec = 40,
                    star2TimeSec = 75
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 2, row = 2, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 5, row = 4, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2200,
                targetScore2Star = 4200,
                targetScore3Star = 6500,
                moveBudgetStar2 = 16,
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
            stageId = StageId(3, 4),
            blueprint = StageBlueprint(
                stageName = "CRIMSON SYNTHESIS",
                directive = "Synthesize 35 energy tiles while unlocking encryption nodes.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.CHROMA_SYNTHESIS,
                    title = "35 TILES SYNTHESIZED",
                    targetAmount = 35,
                    star3TimeSec = 45,
                    star2TimeSec = 85
                ),
                initialCores = emptyList()
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2500,
                targetScore2Star = 5000,
                targetScore3Star = 7500,
                moveBudgetStar2 = 18,
                timeLimitSecStar2 = 60,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.SCORE_THRESHOLD,
                    targetValue = 5500,
                    description = "Reach 5,500 points during synthesis"
                )
            )
        ),
        // Stage 5: Perimeter Confinement
        StageDefinition(
            stageId = StageId(3, 5),
            blueprint = StageBlueprint(
                stageName = "CIPHER WALL CONFINEMENT",
                directive = "Purge 3 Crimson Ciphers lined up along the northern perimeter.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "3 CIPHERS PURGED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 95
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 3, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 5, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 2800,
                targetScore2Star = 5800,
                targetScore3Star = 8800,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 70,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 6: Momentum Harmonic
        StageDefinition(
            stageId = StageId(3, 6),
            blueprint = StageBlueprint(
                stageName = "ENCRYPTION OVERHEAT",
                directive = "Sustain a 3x Surge Streak to breach the reinforced cipher core.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.SURGE_STREAK_TARGET,
                    title = "3X SURGE STREAK REACHED",
                    targetAmount = 3,
                    star3TimeSec = 50,
                    star2TimeSec = 100
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 4, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 3, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3200,
                targetScore2Star = 6200,
                targetScore3Star = 9500,
                moveBudgetStar2 = 22,
                timeLimitSecStar2 = 75,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Reach a 3x Surge Streak"
                )
            )
        ),
        // Stage 7: Subspace Overclock
        StageDefinition(
            stageId = StageId(3, 7),
            blueprint = StageBlueprint(
                stageName = "QUAD CIPHER CHOKEPOINT",
                directive = "Unlock and purge 4 Crimson Ciphers at asymmetric choke points.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "4 CIPHERS PURGED",
                    targetAmount = 4,
                    star3TimeSec = 55,
                    star2TimeSec = 105
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 1, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 3, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 4, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 2, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 3800,
                targetScore2Star = 7200,
                targetScore3Star = 11000,
                moveBudgetStar2 = 26,
                timeLimitSecStar2 = 85,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MULTI_LINE_CLEAR,
                    targetValue = 2,
                    description = "Execute a multi-line clear"
                )
            )
        ),
        // Stage 8: Dual-Wave Escalation
        StageDefinition(
            stageId = StageId(3, 8),
            blueprint = StageBlueprint(
                stageName = "DUAL WAVE CIPHER ESCALATION",
                directive = "Purge 5 Crimson Ciphers across 2 defensive waves.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "5 CIPHERS PURGED (2 WAVES)",
                    targetAmount = 5,
                    star3TimeSec = 65,
                    star2TimeSec = 115
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
                moveBudgetStar2 = 28,
                timeLimitSecStar2 = 95,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.MIN_COMBO_STREAK,
                    targetValue = 3,
                    description = "Sustain a 3x Surge Streak"
                )
            )
        ),
        // Stage 9: Sector Apex Boss
        StageDefinition(
            stageId = StageId(3, 9),
            blueprint = StageBlueprint(
                stageName = "CIPHER OVERLORD // APEX",
                directive = "Phase 1: Unlock and purge 4 Cipher Pylons.\nPhase 2: Strike the central Master Cipher 3x.",
                objective = AdventureStageObjective(
                    type = ObjectiveType.INFECTED_PURGE,
                    title = "CIPHER OVERLORD DEFEATED",
                    targetAmount = 5,
                    star3TimeSec = 85,
                    star2TimeSec = 135
                ),
                initialCores = listOf(
                    CorePlacementSpec(col = 3, row = 3, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 3, isLocked = true),
                    CorePlacementSpec(col = 1, row = 2, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 1, isLocked = true),
                    CorePlacementSpec(col = 6, row = 1, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 1, isLocked = true),
                    CorePlacementSpec(col = 1, row = 6, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 1, isLocked = true),
                    CorePlacementSpec(col = 6, row = 5, coreType = SectorCoreType.CRIMSON_CIPHER_SEC3, maxHits = 1, isLocked = true)
                )
            ),
            benchmarks = StageBenchmarks(
                targetScore1Star = 5000,
                targetScore2Star = 8800,
                targetScore3Star = 13500,
                moveBudgetStar2 = 30,
                timeLimitSecStar2 = 100,
                masteryFeat = MasteryFeatSpec(
                    featType = MasteryFeatType.NO_EMP_JAMMED,
                    targetValue = 1,
                    description = "Defeat Overlord with 0 slots jammed"
                )
            )
        )
    )
)
