package com.example.gridsurge.game.juice

import androidx.compose.runtime.Immutable

enum class DangerLevel {
    SAFE,       // 0 pieces blocked; ample board clearance
    WARNING,    // 1 dock piece has 0 legal placements (Amber 2 Hz breath)
    CRITICAL    // 2+ dock pieces have 0 legal placements (Crimson 6 Hz heartbeat)
}

@Immutable
data class BoardPressureState(
    val dangerLevel: DangerLevel = DangerLevel.SAFE,
    val unplaceableCount: Int = 0,
    val totalDockPieces: Int = 3
)
