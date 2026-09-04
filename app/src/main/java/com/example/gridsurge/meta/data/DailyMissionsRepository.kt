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
