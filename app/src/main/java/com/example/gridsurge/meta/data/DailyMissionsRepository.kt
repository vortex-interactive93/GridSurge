package com.example.gridsurge.meta.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.missionDataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_mission_prefs")

class DailyMissionsRepository(private val context: Context) {

    private fun progressKey(missionId: String) = intPreferencesKey("mission_progress_$missionId")
    private fun claimedKey(missionId: String) = booleanPreferencesKey("mission_claimed_$missionId")

    private val missionTargets = mapOf(
        "q1" to 5,
        "q2" to 30,
        "q3" to 5,
        "q4" to 10000,
        "q5" to 2,
        "dir_place_blocks" to 50,
        "dir_fever_surge" to 3,
        "dir_clear_lines" to 12,
        "dir_anomaly_seed" to 5
    )

    fun getClaimableMissionsCount(): Flow<Int> = context.missionDataStore.data.map { prefs ->
        var count = 0
        missionTargets.forEach { (id, target) ->
            val progress = prefs[progressKey(id)] ?: 0
            val claimed = prefs[claimedKey(id)] ?: false
            if (progress >= target && !claimed) {
                count++
            }
        }
        count
    }

    fun getMissionProgress(missionId: String): Flow<Int> = context.missionDataStore.data.map { prefs ->
        prefs[progressKey(missionId)] ?: 0
    }

    fun isMissionClaimed(missionId: String): Flow<Boolean> = context.missionDataStore.data.map { prefs ->
        prefs[claimedKey(missionId)] ?: false
    }

    suspend fun updateProgress(missionId: String, progress: Int) {
        context.missionDataStore.edit { prefs ->
            prefs[progressKey(missionId)] = progress
        }
    }

    suspend fun incrementProgress(missionId: String, delta: Int) {
        context.missionDataStore.edit { prefs ->
            val current = prefs[progressKey(missionId)] ?: 0
            prefs[progressKey(missionId)] = current + delta
        }
    }

    suspend fun setMissionClaimed(missionId: String, claimed: Boolean) {
        context.missionDataStore.edit { prefs ->
            prefs[claimedKey(missionId)] = claimed
        }
    }
}
