package com.example.gridsurge.operations.data

import com.example.gridsurge.operations.model.*
import com.example.gridsurge.ui.Screen

object OperationsCatalog {
    val DEFAULT_MILESTONES = listOf(
        MilestoneCacheState(MilestoneTier.ALPHA, isClaimed = false),
        MilestoneCacheState(MilestoneTier.BETA, isClaimed = false),
        MilestoneCacheState(MilestoneTier.GAMMA, isClaimed = false),
        MilestoneCacheState(MilestoneTier.APEX, isClaimed = false)
    )

    val DEFAULT_DIRECTIVES = listOf(
        OperationDirective(
            id = "dir_place_blocks",
            title = "NEURAL PLACEMENT",
            description = "Drop 50 polyomino units into the active grid matrix.",
            difficulty = DirectiveDifficulty.STANDARD,
            currentProgress = 0,
            targetProgress = 50,
            rewardPoints = 20,
            rewardStars = 30,
            targetScreen = Screen.GAME_CLASSIC,
            status = DirectiveStatus.IN_PROGRESS
        ),
        OperationDirective(
            id = "dir_fever_surge",
            title = "OVERDRIVE VELOCITY",
            description = "Trigger maximum 2.0x Fever Surge state 3 times.",
            difficulty = DirectiveDifficulty.TACTICAL,
            currentProgress = 0,
            targetProgress = 3,
            rewardPoints = 30,
            rewardStars = 75,
            targetScreen = Screen.TIME_BLITZ,
            status = DirectiveStatus.IN_PROGRESS
        ),
        OperationDirective(
            id = "dir_clear_lines",
            title = "GRID DECONTAMINATION",
            description = "Clear 12 simultaneous multi-line cluster bounties.",
            difficulty = DirectiveDifficulty.STANDARD,
            currentProgress = 0,
            targetProgress = 12,
            rewardPoints = 20,
            rewardStars = 50,
            targetScreen = Screen.GAME_CLASSIC,
            status = DirectiveStatus.IN_PROGRESS
        ),
        OperationDirective(
            id = "dir_anomaly_seed",
            title = "GLITCH HARVEST",
            description = "Survive 5 corrupted glitch pulses in the Daily Glitch run.",
            difficulty = DirectiveDifficulty.ANOMALY,
            currentProgress = 0,
            targetProgress = 5,
            rewardPoints = 40,
            rewardStars = 120,
            targetScreen = Screen.DAILY_GLITCH,
            status = DirectiveStatus.IN_PROGRESS
        )
    )
}
