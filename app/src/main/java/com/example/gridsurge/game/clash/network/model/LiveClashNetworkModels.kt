package com.example.gridsurge.game.clash.network.model

import com.example.gridsurge.game.clash.model.RivalCombatant
import com.example.gridsurge.game.replay.MatchReplayData
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class LiveMatchmakingPhase {
    IDLE,
    CONNECTING_TO_LOBBY,
    SEARCHING_QUEUE,            // Active radar sweep; expanding MMR window
    MATCH_RESERVED,             // Paired with opponent; running NTP ping-pong
    PREPARING_STAGE,            // Shared seed applied; dock pre-populated in background
    STAGING_COUNTDOWN,          // Synced 3.. 2.. 1.. combat staging
    DUEL_ACTIVE,                // Synchronized 90s battle
    OPPONENT_DISCONNECTED,      // Forfeit / Technical Knockout
    ERROR_TIMEOUT
}

@Serializable
data class MatchmakingTicket(
    val playerId: String,
    val callsign: String,
    val ratingMmr: Int,
    val clientVersion: String = "2.4.0",
    val region: String = "US-EAST"
)

@Serializable
data class LiveMatchSessionConfig(
    val roomId: String,
    val sharedMatchSeed: Long,
    val matchStartEpochMs: Long,
    val totalMatchSeconds: Float = 90.0f,
    @Contextual val rivalProfile: RivalCombatant? = null
)

@Serializable
data class MatchFindResponse(
    @SerialName("status") val status: String,
    @SerialName("room_id") val room_id: String? = null,
    @SerialName("shared_seed") val shared_seed: Long? = null,
    @SerialName("start_epoch_ms") val start_epoch_ms: Long? = null,
    @SerialName("rival_id") val rival_id: String? = null,
    @SerialName("rival_callsign") val rival_callsign: String? = null,
    @SerialName("rival_mmr") val rival_mmr: Int? = null,
    @SerialName("rival_avatar_key") val rival_avatar_key: String? = null,
    @SerialName("rival_equipped_badges") val rival_equipped_badges: String? = null,
    @Contextual val ghost_replay: MatchReplayData? = null
) {
    val roomId: String? get() = room_id
    val sharedSeed: Long? get() = shared_seed
    val startEpochMs: Long? get() = start_epoch_ms
    val rivalCallsign: String? get() = rival_callsign
    val rivalMmr: Int? get() = rival_mmr
    val rivalAvatarKey: String? get() = rival_avatar_key
    val rivalEquippedBadges: String? get() = rival_equipped_badges
}

@Serializable
data class LiveCombatPacket(
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("score") val score: Long = 0L,
    @SerialName("lines") val lines: Int = 0,
    @SerialName("combo") val combo: Int = 0,
    @SerialName("fever") val fever: Boolean = false,
    @SerialName("tko") val tko: Boolean = false,
    @SerialName("apm") val apm: Int = 0,
    @SerialName("flushes") val flushes: Int = 0,
    @SerialName("ts") val timestampMs: Long = System.currentTimeMillis()
)

// Network Packets (JSON Payload Schemas)
sealed class ClashNetworkPacket {
    data class QueueJoinRequest(val ticket: MatchmakingTicket) : ClashNetworkPacket()
    data class QueueStatusUpdate(val currentWaitSec: Int, val searchMmrWindow: Int) : ClashNetworkPacket()
    data class MatchFoundPayload(
        val roomId: String,
        val sharedSeed: Long,
        val startEpochMs: Long,
        val rivalCallsign: String,
        val rivalMmr: Int,
        val rivalTier: String
    ) : ClashNetworkPacket()
    
    // Low-Latency In-Game Combat Relay Packet (< 40 bytes)
    data class CombatTelemetrySync(
        val roomId: String,
        val sequenceId: Long,
        val currentScore: Long,
        val linesClearedThisMove: Int,
        val comboStreak: Int,
        val isFeverActive: Boolean,
        val isMatrixToppedOut: Boolean // Early TKO
    ) : ClashNetworkPacket()

    data class HeartbeatPing(val clientTimestampMs: Long) : ClashNetworkPacket()
    data class HeartbeatPong(val clientTimestampMs: Long, val serverTimestampMs: Long) : ClashNetworkPacket()
    data class PlayerForfeit(val roomId: String, val reason: String) : ClashNetworkPacket()
}
