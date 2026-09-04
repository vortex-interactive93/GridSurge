package com.example.gridsurge.game.glitch

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class DailyGlitchTier(val displayName: String, val badgeColor: Color) {
    GRANDMASTER("TOP 1%", Color(0xFFFFD600)),
    MASTER("TOP 5%", Color(0xFFEA80FC)),
    DIAMOND("TOP 20%", Color(0xFF00E5FF)),
    GOLD("TOP 50%", Color(0xFF00FF66)),
    BRONZE("PARTICIPANT", Color(0xFF8A99AD))
}

@Immutable
data class DailyLeaderboardEntry(
    val rank: Int,
    val callsign: String,
    val score: Long,
    val wavesCleared: Int,
    val tier: DailyGlitchTier,
    val isCurrentUser: Boolean = false
)

@Immutable
data class DailyGlitchUiState(
    val seedDateFormatted: String,
    val timeRemainingMillis: Long,
    val formattedTimeRemaining: String,
    val hasTicketAvailable: Boolean,
    val userPersonalBestScore: Long,
    val userPersonalBestWaves: Int,
    val userRank: Int?,
    val retryStarCost: Int = 100,
    val leaderboardPreview: List<DailyLeaderboardEntry>,
    val userEntry: DailyLeaderboardEntry?
)
