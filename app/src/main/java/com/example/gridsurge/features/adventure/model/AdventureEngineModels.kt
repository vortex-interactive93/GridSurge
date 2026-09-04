package com.example.gridsurge.features.adventure.model

import androidx.compose.runtime.Immutable

enum class MatchPhase {
    IN_PROGRESS,
    OBJECTIVE_MET_PLAYING_OUT, // Target met; player plays remaining turns for high scores
    STAGE_COMPLETED,           // Moves exhausted or board locked with target achieved
    STAGE_FAILED               // Moves exhausted without achieving target
}

@Immutable
data class AdventureRuntimeState(
    val levelSpec: LevelNodeSpec,
    val remainingMoves: Int,
    val currentScore: Long = 0L,
    val linesClearedCount: Int = 0,
    val nodesPurgedCount: Int = 0,
    val isObjectiveMet: Boolean = false,
    val calculatedStars: Int = 0,
    val matchPhase: MatchPhase = MatchPhase.IN_PROGRESS
)
