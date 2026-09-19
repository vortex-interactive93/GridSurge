package com.example.gridsurge.operations.model

import androidx.compose.ui.graphics.Color
import com.example.gridsurge.ui.Screen

enum class DirectiveDifficulty(
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    STANDARD("STANDARD DIRECTIVE", Color(0xFF00E5FF), Color(0xFF003840)),
    TACTICAL("TACTICAL BOUNTY", Color(0xFFFFB300), Color(0xFF4A3400)),
    ANOMALY("GLITCH ANOMALY", Color(0xFFFF1744), Color(0xFF4A0010))
}

enum class DirectiveStatus {
    IN_PROGRESS,
    READY_TO_CLAIM,
    CLAIMED
}

data class OperationDirective(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: DirectiveDifficulty,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardPoints: Int,
    val rewardStars: Int,
    val targetScreen: Screen = Screen.GAME_CLASSIC,
    val status: DirectiveStatus = DirectiveStatus.IN_PROGRESS
)

enum class MilestoneTier(val targetPoints: Int, val starReward: Int, val cacheTitle: String) {
    ALPHA(25, 50, "ALPHA CACHE"),
    BETA(50, 100, "BETA CRATE"),
    GAMMA(75, 150, "GAMMA POD"),
    APEX(100, 300, "APEX VAULT")
}

data class MilestoneCacheState(
    val tier: MilestoneTier,
    val isClaimed: Boolean = false
)

data class DailyOperationsState(
    val currentOpsPoints: Int = 0,
    val maxOpsPoints: Int = 100,
    val resetCountdownFormatted: String = "08:14:22",
    val directives: List<OperationDirective> = emptyList(),
    val milestoneStates: List<MilestoneCacheState> = emptyList()
)
