package com.example.gridsurge.meta

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CampaignProgressManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gridsurge_campaign", Context.MODE_PRIVATE)

    private val _totalStars = MutableStateFlow(calculateTotalStars())
    val totalStars: StateFlow<Int> = _totalStars.asStateFlow()

    private val _highestUnlockedLevel = MutableStateFlow(prefs.getInt(KEY_HIGHEST_UNLOCKED_LEVEL, 1))
    val highestUnlockedLevel: StateFlow<Int> = _highestUnlockedLevel.asStateFlow()

    fun getStarsForLevel(levelId: Int): Int = prefs.getInt("${KEY_LEVEL_STARS_PREFIX}$levelId", 0)

    fun getHighScoreForLevel(levelId: Int): Int = prefs.getInt("${KEY_LEVEL_SCORE_PREFIX}$levelId", 0)

    fun isMasteryChestClaimed(sectorIndex: Int): Boolean = prefs.getBoolean("${KEY_CHEST_CLAIMED_PREFIX}$sectorIndex", false)

    fun recordLevelResult(levelId: Int, score: Int, starsEarned: Int): Boolean {
        val currentStars = getStarsForLevel(levelId)
        val currentBest = getHighScoreForLevel(levelId)

        val editor = prefs.edit()
        if (score > currentBest) {
            editor.putInt("${KEY_LEVEL_SCORE_PREFIX}$levelId", score)
        }

        var isNewUnlock = false
        if (starsEarned > currentStars) {
            editor.putInt("${KEY_LEVEL_STARS_PREFIX}$levelId", starsEarned)
        }

        // If at least 1 star was earned, unlock the next node
        if (starsEarned > 0 && levelId == _highestUnlockedLevel.value) {
            val nextLevel = levelId + 1
            editor.putInt(KEY_HIGHEST_UNLOCKED_LEVEL, nextLevel)
            _highestUnlockedLevel.value = nextLevel
            isNewUnlock = true
        }

        editor.apply()
        _totalStars.value = calculateTotalStars()
        return isNewUnlock
    }

    fun claimMasteryChest(sectorIndex: Int, rewardStars: Int, playerProfileManager: PlayerProfileManager): Boolean {
        if (isMasteryChestClaimed(sectorIndex)) return false
        prefs.edit().putBoolean("${KEY_CHEST_CLAIMED_PREFIX}$sectorIndex", true).apply()
        playerProfileManager.addStarCurrency(rewardStars)
        return true
    }

    private fun calculateTotalStars(): Int {
        var stars = 0
        for (i in 1..30) {
            stars += prefs.getInt("${KEY_LEVEL_STARS_PREFIX}$i", 0)
        }
        return stars
    }

    companion object {
        private const val KEY_HIGHEST_UNLOCKED_LEVEL = "key_highest_unlocked_level"
        private const val KEY_LEVEL_STARS_PREFIX = "level_stars_"
        private const val KEY_LEVEL_SCORE_PREFIX = "level_score_"
        private const val KEY_CHEST_CLAIMED_PREFIX = "chest_claimed_"
    }
}
