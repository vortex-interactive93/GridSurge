package com.example.gridsurge.clash.data

import android.util.Log
import com.example.gridsurge.game.replay.MatchReplayData
import com.example.gridsurge.game.replay.ReplayMove
import com.example.gridsurge.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseReplayRow(
    val id: String? = null,
    val callsign: String,
    val final_score: Long,
    val match_duration_sec: Int,
    val replay_code: String
)

object ClashReplayRepository {

    private const val TAG = "ClashReplayRepository"
    private const val TABLE_NAME = "clash_replays"

    val DEFAULT_RIVAL_GHOST_REPLAY = MatchReplayData(
        matchId = "default_ghost_01",
        matchSeed = 1001L,
        gameMode = "BLITZ_CLASH",
        matchDurationSec = 75,
        finalPlayerScore = 18450L,
        finalRivalScore = 18450L,
        isVictory = false,
        playerMoves = listOf(
            ReplayMove(2000L, 0, 1, 6, 2, listOf(0 to 0), 1, 0, 150L, 1),
            ReplayMove(4200L, 1, 2, 6, 3, listOf(0 to 0, 0 to 1), 2, 0, 320L, 1),
            ReplayMove(6800L, 0, 3, 7, 0, listOf(0 to 0, 0 to 1, 0 to 2), 1, 1, 680L, 2),
            ReplayMove(9500L, 2, 1, 5, 4, listOf(0 to 0), 3, 0, 850L, 1),
            ReplayMove(12100L, 0, 4, 4, 2, listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1), 4, 0, 1120L, 1),
            ReplayMove(14800L, 1, 2, 7, 3, listOf(0 to 0, 0 to 1), 2, 1, 1650L, 2),
            ReplayMove(17500L, 2, 3, 3, 1, listOf(0 to 0, 1 to 0, 2 to 0), 1, 0, 1890L, 1),
            ReplayMove(20200L, 0, 1, 2, 5, listOf(0 to 0), 3, 0, 2150L, 1),
            ReplayMove(23000L, 1, 5, 7, 5, listOf(0 to 0, 0 to 1, 0 to 2), 5, 2, 3100L, 3),
            ReplayMove(25800L, 0, 2, 5, 0, listOf(0 to 0, 0 to 1), 1, 0, 3350L, 1),
            ReplayMove(28600L, 2, 3, 1, 2, listOf(0 to 0, 0 to 1, 0 to 2), 2, 0, 3620L, 1),
            ReplayMove(31400L, 1, 4, 6, 6, listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1), 4, 1, 4450L, 2),
            ReplayMove(34200L, 0, 1, 0, 0, listOf(0 to 0), 1, 0, 4650L, 1),
            ReplayMove(37000L, 2, 2, 4, 5, listOf(0 to 0, 0 to 1), 3, 0, 4920L, 1),
            ReplayMove(39800L, 1, 5, 7, 0, listOf(0 to 0, 0 to 1, 0 to 2), 2, 2, 6100L, 3),
            ReplayMove(42600L, 0, 3, 3, 4, listOf(0 to 0, 1 to 0, 2 to 0), 1, 0, 6400L, 1),
            ReplayMove(45400L, 2, 1, 1, 6, listOf(0 to 0), 5, 0, 6680L, 1),
            ReplayMove(48200L, 1, 4, 5, 2, listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1), 4, 1, 7600L, 2),
            ReplayMove(51000L, 0, 2, 2, 0, listOf(0 to 0, 0 to 1), 2, 0, 7920L, 1),
            ReplayMove(53800L, 2, 5, 7, 2, listOf(0 to 0, 0 to 1, 0 to 2), 3, 2, 9400L, 3),
            ReplayMove(56600L, 1, 3, 0, 4, listOf(0 to 0, 0 to 1, 0 to 2), 1, 0, 9750L, 1),
            ReplayMove(59400L, 0, 1, 4, 0, listOf(0 to 0), 2, 0, 10100L, 1),
            ReplayMove(62200L, 2, 4, 6, 2, listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1), 5, 2, 11800L, 2),
            ReplayMove(65000L, 1, 2, 1, 1, listOf(0 to 0, 0 to 1), 3, 0, 12200L, 1),
            ReplayMove(67800L, 0, 5, 7, 4, listOf(0 to 0, 0 to 1, 0 to 2), 4, 3, 14900L, 4),
            ReplayMove(70600L, 2, 3, 3, 3, listOf(0 to 0, 0 to 1, 0 to 2), 1, 1, 16200L, 2),
            ReplayMove(73400L, 1, 2, 0, 1, listOf(0 to 0, 0 to 1), 2, 2, 18450L, 3)
        ),
        rivalMoves = emptyList()
    )

    /**
     * Uploads a finished match replay to Supabase clash_replays table.
     */
    suspend fun uploadReplay(callsign: String, replayData: MatchReplayData): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) {
            Log.d(TAG, "Supabase un-configured; skipping online upload.")
            return@withContext false
        }

        try {
            val code = replayData.toReplayCode()
            val row = SupabaseReplayRow(
                id = replayData.matchId,
                callsign = callsign,
                final_score = replayData.finalPlayerScore,
                match_duration_sec = replayData.matchDurationSec,
                replay_code = code
            )

            SupabaseClientProvider.client.from(TABLE_NAME).insert(row)
            Log.d(TAG, "Successfully uploaded match replay ${replayData.matchId} to Supabase!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed uploading replay to Supabase: ${e.message}")
            false
        }
    }

    /**
     * Fetches a random rival player's recorded replay from Supabase, or returns the built-in default ghost.
     */
    suspend fun fetchRandomRivalReplay(): MatchReplayData = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val rows = SupabaseClientProvider.client.from(TABLE_NAME)
                    .select()
                    .decodeList<SupabaseReplayRow>()

                if (rows.isNotEmpty()) {
                    val randomRow = rows.random()
                    val replay = MatchReplayData.fromReplayCode(randomRow.replay_code)
                    if (replay != null) {
                        Log.d(TAG, "Fetched real rival replay from Supabase! Callsign: ${randomRow.callsign}, Score: ${randomRow.final_score}")
                        return@withContext replay
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rival replay from Supabase; using default ghost: ${e.message}")
            }
        }
        DEFAULT_RIVAL_GHOST_REPLAY
    }
}
