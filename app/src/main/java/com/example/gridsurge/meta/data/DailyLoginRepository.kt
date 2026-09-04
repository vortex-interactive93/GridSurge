package com.example.gridsurge.meta.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.gridsurge.meta.PlayerProfileManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val Context.dailyLoginDataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_login_prefs")

data class DailyLoginState(
    val currentStreak: Int,
    val canClaimToday: Boolean,
    val lastLoginEpochDay: Long
)

class DailyLoginRepository(
    private val context: Context,
    private val profileManager: PlayerProfileManager
) {
    private object PreferencesKeys {
        val LAST_LOGIN_EPOCH_DAY = longPreferencesKey("last_login_epoch_day")
        val CONSECUTIVE_STREAK = intPreferencesKey("consecutive_streak")
    }

    val loginStateFlow: Flow<DailyLoginState> = context.dailyLoginDataStore.data.map { prefs ->
        val lastLogin = prefs[PreferencesKeys.LAST_LOGIN_EPOCH_DAY] ?: 0L
        val streak = prefs[PreferencesKeys.CONSECUTIVE_STREAK] ?: 0
        val today = LocalDate.now().toEpochDay()
        
        val canClaim = today > lastLogin
        
        DailyLoginState(
            currentStreak = calculateCurrentStreak(lastLogin, streak, today),
            canClaimToday = canClaim,
            lastLoginEpochDay = lastLogin
        )
    }

    private fun calculateCurrentStreak(lastLogin: Long, streak: Int, today: Long): Int {
        if (lastLogin == 0L) return 1
        val daysBetween = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(lastLogin), LocalDate.ofEpochDay(today))
        
        return when {
            daysBetween <= 0 -> streak // Same day
            daysBetween == 1L -> if (streak >= 7) 1 else streak + 1 // Consecutive
            else -> 1 // Streak broken
        }
    }

    suspend fun claimReward(rewardStars: Int) {
        val today = LocalDate.now().toEpochDay()
        val currentState = loginStateFlow.first()
        
        if (currentState.canClaimToday) {
            context.dailyLoginDataStore.edit { prefs ->
                prefs[PreferencesKeys.LAST_LOGIN_EPOCH_DAY] = today
                prefs[PreferencesKeys.CONSECUTIVE_STREAK] = currentState.currentStreak
            }
            profileManager.addStarCurrency(rewardStars)
        }
    }
}
