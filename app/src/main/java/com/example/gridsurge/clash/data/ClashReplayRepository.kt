package com.example.gridsurge.clash.data

import android.util.Log
import com.example.gridsurge.game.replay.MatchReplayData
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
     * Fetches a random rival player's recorded replay from Supabase.
     */
    suspend fun fetchRandomRivalReplay(): MatchReplayData? = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) return@withContext null

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
            Log.e(TAG, "Error fetching rival replay from Supabase: ${e.message}")
        }
        null
    }
}
