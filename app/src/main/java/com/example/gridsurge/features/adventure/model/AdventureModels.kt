package com.example.gridsurge.features.adventure.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class ObjectiveType(val title: String, val unitSuffix: String) {
    SCORE_ATTACK("REACH TARGET SCORE", "PTS"),
    LINE_CLEANSE("CLEAR TOTAL LINES", "LINES"),
    INFECTED_PURGE("PURGE GLITCH NODES", "NODES"),
    SURVIVOR("SURVIVE MOVE LIMIT", "MOVES"),
    CHROMA_SYNTHESIS("CHROMA SYNTHESIS", "TILES"),
    SURGE_STREAK_TARGET("SURGE STREAK", "STREAK"),
    MOVE_BUDGET_SWEEP("MOVES REMAINING", "MOVES")
}

enum class AdventureHazardType {
    NONE,
    CRYO_FROST,     // Requires adjacent line clear to break
    MOLTEN_SLAG,    // Explodes and wipes row if not cleared within N moves
    EMP_LOCK        // Locks grid slot from receiving polyominoes
}

enum class BossPhaseState {
    DORMANT,
    SHIELDED,       // Invulnerable until all corner pylons are purged
    OVERCHARGING,   // Charging a board wipe attack (3 moves remaining)
    VULNERABLE,     // Exposed to direct line-clear damage
    DEFEATED
}

@Immutable
data class HazardCellState(
    val hazardType: AdventureHazardType = AdventureHazardType.NONE,
    val countdownMoves: Int = 0,
    val isFrozen: Boolean = false
)

@Immutable
data class BossThreatState(
    val bossId: String = "NEON_GUARDIAN",
    val phase: BossPhaseState = BossPhaseState.SHIELDED,
    val maxHp: Int = 4,
    val currentHp: Int = 4,
    val pylonsRemaining: Int = 4,
    val movesUntilOvercharge: Int = 5,
    val activeTetherTargetCoords: List<Pair<Int, Int>> = emptyList()
)

@Immutable
data class BalancedLevelObjective(
    val type: ObjectiveType,
    val targetAmount: Int,
    val star3TimeSec: Int,
    val star2TimeSec: Int,
    val maxMovesAllowed: Int,
    val minComboRequired: Int = 0,
    val hasBossThreat: Boolean = false
)

@Immutable
data class LevelObjective(
    val type: ObjectiveType,
    val targetAmount: Int,
    val maxMovesAllowed: Int,
    val initialInfectedCoresCount: Int = 0,
    val starThresholds: List<Int> // [1-Star, 2-Star, 3-Star Score Thresholds]
)

@Immutable
data class LevelNodeSpec(
    val levelNumber: Int,         // Global index 1..40
    val sectorIndex: Int,         // 1..4
    val levelInSector: Int,       // 1..10
    val title: String,
    val objective: LevelObjective,
    val normalizedX: Float,       // 0.15f to 0.85f (Horizontal coordinate on path)
    val isBossLevel: Boolean = false,
    val initialBoardLayout: List<Int>? = null // 64-element array: 0=Empty, 1=Settled Block, 9=Infected Node
)

@Immutable
data class SectorSpec(
    val sectorId: Int,
    val codename: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val requiredStarsToUnlock: Int,
    val backgroundDrawableRes: Int,
    val levels: List<LevelNodeSpec>
)

@Immutable
data class LevelProgressRecord(
    val levelNumber: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0, // 0..3
    val highScore: Long = 0L,
    val bestTimeSec: Long = 0L
)

@Immutable
data class AdventureMapUiState(
    val currentSectorIndex: Int = 1,
    val totalStarsCollected: Int = 0,
    val activeSector: SectorSpec,
    val allSectors: List<SectorSpec>,
    val progressMap: Map<Int, LevelProgressRecord>,
    val selectedLevel: LevelNodeSpec? = null
)
