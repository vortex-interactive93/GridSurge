package com.example.gridsurge.features.adventure.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.gridsurge.features.adventure.model.LevelProgressRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.adventureDataStore: DataStore<Preferences> by preferencesDataStore(name = "gridsurge_adventure_prefs")

class AdventureRepository(private val context: Context) {

    val progressFlow: Flow<Map<Int, LevelProgressRecord>> = context.adventureDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val map = mutableMapOf<Int, LevelProgressRecord>()

            // Stage 1 default initialization
            val lvl1Completed = prefs[booleanPreferencesKey("lvl_1_completed")] ?: false
            map[1] = LevelProgressRecord(
                levelNumber = 1,
                isUnlocked = true,
                isCompleted = lvl1Completed,
                starsEarned = prefs[intPreferencesKey("lvl_1_stars")] ?: 0,
                highScore = prefs[longPreferencesKey("lvl_1_score")] ?: 0L,
                bestTimeSec = prefs[longPreferencesKey("lvl_1_time")] ?: 0L
            )

            // Dynamic evaluation for Stages 2..40
            for (lvl in 2..40) {
                val isPrevCompleted = map[lvl - 1]?.isCompleted ?: false
                val explicitlyUnlocked = prefs[booleanPreferencesKey("lvl_${lvl}_unlocked")] ?: false
                val isCompleted = prefs[booleanPreferencesKey("lvl_${lvl}_completed")] ?: false
                val stars = prefs[intPreferencesKey("lvl_${lvl}_stars")] ?: 0
                val score = prefs[longPreferencesKey("lvl_${lvl}_score")] ?: 0L
                val time = prefs[longPreferencesKey("lvl_${lvl}_time")] ?: 0L

                map[lvl] = LevelProgressRecord(
                    levelNumber = lvl,
                    isUnlocked = isPrevCompleted || isCompleted || explicitlyUnlocked,
                    isCompleted = isCompleted,
                    starsEarned = stars,
                    highScore = score,
                    bestTimeSec = time
                )
            }
            map
        }

    fun isRelicClaimed(sectorId: Int): Flow<Boolean> = context.adventureDataStore.data
        .map { prefs -> prefs[booleanPreferencesKey("sector_${sectorId}_relic_claimed")] ?: false }

    fun getClaimedRelicSectors(): Flow<Set<Int>> = context.adventureDataStore.data
        .map { prefs ->
            val claimed = mutableSetOf<Int>()
            for (sectorId in 1..5) {
                if (prefs[booleanPreferencesKey("sector_${sectorId}_relic_claimed")] == true) {
                    claimed.add(sectorId)
                }
            }
            claimed
        }

    suspend fun recordRelicClaim(sectorId: Int) {
        context.adventureDataStore.edit { prefs ->
            prefs[booleanPreferencesKey("sector_${sectorId}_relic_claimed")] = true
        }
    }

    suspend fun recordLevelCompletion(levelNumber: Int, score: Long, starsEarned: Int, timeSec: Long) {
        context.adventureDataStore.edit { prefs ->
            val prevStars = prefs[intPreferencesKey("lvl_${levelNumber}_stars")] ?: 0
            val prevScore = prefs[longPreferencesKey("lvl_${levelNumber}_score")] ?: 0L
            val prevTime = prefs[longPreferencesKey("lvl_${levelNumber}_time")] ?: 0L

            prefs[booleanPreferencesKey("lvl_${levelNumber}_completed")] = true
            prefs[intPreferencesKey("lvl_${levelNumber}_stars")] = maxOf(prevStars, starsEarned)
            prefs[longPreferencesKey("lvl_${levelNumber}_score")] = maxOf(prevScore, score)
            
            if (prevTime == 0L || timeSec < prevTime) {
                prefs[longPreferencesKey("lvl_${levelNumber}_time")] = timeSec
            }

            // Explicitly unlock next level
            val nextLevel = levelNumber + 1
            if (nextLevel <= 40) {
                prefs[booleanPreferencesKey("lvl_${nextLevel}_unlocked")] = true
            }
        }
    }
}
