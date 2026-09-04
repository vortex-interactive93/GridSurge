package com.example.gridsurge.leaderboard.model

import androidx.compose.runtime.Immutable
import com.example.gridsurge.game.glitch.DailyGlitchTier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class GameModeType(val storageKey: String, val displayName: String) {
    DAILY_GLITCH("daily_glitch", "DAILY GLITCH"),
    TIME_BLITZ("time_blitz", "TIME BLITZ"),
    CLASSIC_SURGE("classic_surge", "CLASSIC SURGE")
}

/**
 * Packed binary move representation for zero-allocation telemetry.
 * Memory footprint: 12 bytes per move.
 */
@Serializable
data class MoveRecord(
    val moveNumber: Int,
    val trayIndex: Byte,      // 0, 1, or 2
    val anchorCol: Byte,      // 0..7
    val anchorRow: Byte,      // 0..7
    val shapeId: Short,       // Catalog ID reference
    val timestampMs: Long     // Delta from match start
)

@Serializable
data class MatchReplayEnvelope(
    val matchId: String,
    val userId: String,
    val callsign: String,
    val mode: String,
    val seed: Long,
    val claimedScore: Long,
    val totalLinesCleared: Int,
    val maxComboReached: Int,
    val durationMs: Long,
    val moves: List<MoveRecord>,
    val clientSignature: String
)

@Serializable
@Immutable
data class CloudLeaderboardEntry(
    @SerialName("rank") val rank: Int = 0,
    @SerialName("user_id") val userId: String = "",
    @SerialName("callsign") val callsign: String = "",
    @SerialName("score") val score: Long = 0L,
    @SerialName("waves_or_lines") val wavesOrLines: Int = 0,
    @SerialName("mode") val mode: String = "",
    @SerialName("tier") val tier: String = "BRONZE",
    @SerialName("created_at") val timestampUtc: Long = 0L,
    @SerialName("verified") val verified: Boolean = false,
    @SerialName("title") val title: String = "",
    @SerialName("badge_res_id") val badgeResId: Int = 0,
    val isCurrentUser: Boolean = false
) {
    val parsedTier: DailyGlitchTier
        get() = try {
            DailyGlitchTier.valueOf(tier.uppercase())
        } catch (e: Exception) {
            DailyGlitchTier.BRONZE
        }
}

sealed interface LeaderboardSyncState {
    object Idle : LeaderboardSyncState
    object Loading : LeaderboardSyncState
    data class Success(
        val topEntries: List<CloudLeaderboardEntry>,
        val currentUserEntry: CloudLeaderboardEntry?,
        val totalCompetitors: Int
    ) : LeaderboardSyncState
    data class Error(val message: String) : LeaderboardSyncState
}
