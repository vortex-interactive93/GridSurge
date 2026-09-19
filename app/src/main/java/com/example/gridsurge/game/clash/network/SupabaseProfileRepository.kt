package com.example.gridsurge.game.clash.network

import android.util.Log
import com.example.gridsurge.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClashProfileDto(
    @SerialName("user_id") val userId: String = "",
    @SerialName("callsign") val callsign: String = "",
    @SerialName("rating_mmr") val ratingMmr: Int = 1000,
    @SerialName("tier_title") val tierTitle: String = "GOLD I",
    @SerialName("avatar_id") val avatarId: String = "avatar_caucasian_male",
    @SerialName("equipped_badges") val equippedBadges: List<String> = listOf("DECA_SURGE", "GRID_NULLIFIER", "FOUNDER"),
    @SerialName("matches_played") val matchesPlayed: Int = 0,
    @SerialName("victories") val victories: Int = 0,
    @SerialName("defeats") val defeats: Int = 0
)

@Serializable
data class AvatarUpdatePayload(
    @SerialName("avatar_id") val avatarId: String
)

@Serializable
data class BadgeUpdatePayload(
    @SerialName("equipped_badges") val equippedBadges: List<String>
)

object SupabaseProfileRepository {
    private const val TAG = "SUPABASE_PROFILE"

    suspend fun syncProfileToSupabase(
        userId: String,
        callsign: String,
        ratingMmr: Int,
        tierTitle: String = "GOLD I",
        avatarId: String = "avatar_caucasian_male",
        equippedBadges: List<String> = listOf("DECA_SURGE", "GRID_NULLIFIER", "FOUNDER"),
        matchesPlayed: Int = 0,
        victories: Int = 0
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!SupabaseClientProvider.isConfigured) return@runCatching

            val dto = ClashProfileDto(
                userId = userId,
                callsign = callsign,
                ratingMmr = ratingMmr,
                tierTitle = tierTitle,
                avatarId = avatarId,
                equippedBadges = equippedBadges.take(3),
                matchesPlayed = matchesPlayed,
                victories = victories
            )

            try {
                SupabaseClientProvider.client.from("clash_profiles").upsert(dto)
                Log.d(TAG, ">>> Profile synced to Supabase clash_profiles for $userId: avatar=$avatarId")
            } catch (e: Exception) {
                Log.w(TAG, "Failed upserting clash_profile to Supabase: ${e.message}")
            }
        }
    }

    suspend fun fetchOpponentProfile(rivalUserId: String): Result<ClashProfileDto> = withContext(Dispatchers.IO) {
        runCatching {
            if (!SupabaseClientProvider.isConfigured || rivalUserId.isBlank()) {
                return@runCatching ClashProfileDto(userId = rivalUserId, callsign = rivalUserId)
            }

            try {
                val profile = SupabaseClientProvider.client.from("clash_profiles")
                    .select {
                        filter {
                            eq("user_id", rivalUserId)
                        }
                    }
                    .decodeSingle<ClashProfileDto>()
                profile
            } catch (e: Exception) {
                Log.w(TAG, "Failed fetching opponent profile for $rivalUserId: ${e.message}")
                ClashProfileDto(userId = rivalUserId, callsign = rivalUserId)
            }
        }
    }
}
