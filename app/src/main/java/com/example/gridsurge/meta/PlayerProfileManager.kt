package com.example.gridsurge.meta

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class PlayerProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("grid_surge_profile", Context.MODE_PRIVATE)

    private val _highScore = MutableStateFlow(prefs.getInt("high_score", 0))
    val highScore = _highScore.asStateFlow()

    private val _starCurrency = MutableStateFlow(prefs.getInt("star_currency", 25))
    val starCurrency = _starCurrency.asStateFlow()

    private val _equippedBlockSkinId = MutableStateFlow(prefs.getString("equipped_block_skin", "skin_midnight_glass") ?: "skin_midnight_glass")
    val equippedBlockSkinId = _equippedBlockSkinId.asStateFlow()

    private val _equippedVoxPackId = MutableStateFlow(prefs.getString("equipped_vox_pack", "vox_default") ?: "vox_default")
    val equippedVoxPackId = _equippedVoxPackId.asStateFlow()

    private val _unlockedItemIds = MutableStateFlow(prefs.getStringSet("unlocked_items", setOf("skin_midnight_glass", "vox_default")) ?: setOf("skin_midnight_glass", "vox_default"))
    val unlockedItemIds = _unlockedItemIds.asStateFlow()

    private val _isFtueCompleted = MutableStateFlow(prefs.getBoolean("ftue_completed", false))
    val isFtueCompleted = _isFtueCompleted.asStateFlow()

    private val _activeTitle = MutableStateFlow(prefs.getString("active_title", "NEURAL INITIATE") ?: "NEURAL INITIATE")
    val activeTitle = _activeTitle.asStateFlow()

    private val _unlockedTitles = MutableStateFlow(prefs.getStringSet("unlocked_titles", setOf("NEURAL INITIATE")) ?: setOf("NEURAL INITIATE"))
    val unlockedTitles = _unlockedTitles.asStateFlow()

    private val _activeBadgeRes = MutableStateFlow(prefs.getInt("active_badge_res", 0))
    val activeBadgeRes = _activeBadgeRes.asStateFlow()

    private val _unlockedBadgeIds = MutableStateFlow(prefs.getStringSet("unlocked_badges", emptySet()) ?: emptySet<String>())
    val unlockedBadgeIds = _unlockedBadgeIds.asStateFlow()

    private val _ratingPoints = MutableStateFlow(prefs.getInt("rating_points", 0))
    val ratingPoints = _ratingPoints.asStateFlow()

    private val _highestSectorCleared = MutableStateFlow(prefs.getInt("highest_sector_cleared", 0))
    val highestSectorCleared = _highestSectorCleared.asStateFlow()

    private val _hasConfiguredProfile = MutableStateFlow(prefs.getBoolean("has_configured_profile", false))
    val hasConfiguredProfile = _hasConfiguredProfile.asStateFlow()

    private val _callsign = MutableStateFlow(prefs.getString("callsign", "OPERATIVE_X") ?: "OPERATIVE_X")
    val callsign = _callsign.asStateFlow()

    private val _avatarKey = MutableStateFlow(prefs.getString("avatar_key", "avatar_cyber_ninja") ?: "avatar_cyber_ninja")
    val avatarKey = _avatarKey.asStateFlow()

    private val _isNoAdsPurchased = MutableStateFlow(prefs.getBoolean("no_ads_purchased", false))
    val isNoAdsPurchased = _isNoAdsPurchased.asStateFlow()

    fun purchaseNoAdsBundle() {
        val newStars = starCurrency.value + 1000
        prefs.edit().apply {
            putBoolean("no_ads_purchased", true)
            putInt("star_currency", newStars)
        }.apply()

        _isNoAdsPurchased.value = true
        _starCurrency.value = newStars
    }

    fun saveCyberProfile(callsignToSave: String, avatarKeyToSave: String, titleToSave: String) {
        val cleanCallsign = callsignToSave.trim().ifEmpty { "OPERATIVE_X" }
        prefs.edit().apply {
            putBoolean("has_configured_profile", true)
            putString("callsign", cleanCallsign)
            putString("avatar_key", avatarKeyToSave)
            putString("active_title", titleToSave)
        }.apply()

        _hasConfiguredProfile.value = true
        _callsign.value = cleanCallsign
        _avatarKey.value = avatarKeyToSave
        _activeTitle.value = titleToSave
    }

    private val _activeSectorAugmentIds = MutableStateFlow(
        prefs.getStringSet("active_sector_augments", emptySet()) ?: emptySet<String>()
    )
    val activeSectorAugmentIds = _activeSectorAugmentIds.asStateFlow()

    private val _equippedRelicAbilityName = MutableStateFlow(
        prefs.getString("equipped_relic_ability", "NONE") ?: "NONE"
    )
    val equippedRelicAbilityName = _equippedRelicAbilityName.asStateFlow()

    private val _totalRuns = MutableStateFlow(prefs.getInt("total_runs", 0))
    val totalRuns = _totalRuns.asStateFlow()

    private val _maxCombo = MutableStateFlow(prefs.getInt("max_combo", 0))
    val maxCombo = _maxCombo.asStateFlow()

    private val _totalLinesCleared = MutableStateFlow(prefs.getInt("total_lines_cleared", 0))
    val totalLinesCleared = _totalLinesCleared.asStateFlow()

    private val _claimedAchievementIds = MutableStateFlow(
        prefs.getStringSet("claimed_achievements", emptySet()) ?: emptySet<String>()
    )
    val claimedAchievementIds = _claimedAchievementIds.asStateFlow()

    private val _claimedChainTiers = MutableStateFlow<Map<String, Int>>(loadClaimedChainTiers())
    val claimedChainTiers = _claimedChainTiers.asStateFlow()

    private fun loadClaimedChainTiers(): Map<String, Int> {
        val jsonStr = prefs.getString("claimed_chain_tiers_json", "{}") ?: "{}"
        return try {
            val json = JSONObject(jsonStr)
            val map = mutableMapOf<String, Int>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getInt(key)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun claimChainTier(chainId: String, tierLevel: Int, starReward: Int): Boolean {
        val currentTiers = _claimedChainTiers.value.toMutableMap()
        val currentLevel = currentTiers[chainId] ?: 0
        if (tierLevel == currentLevel + 1) {
            currentTiers[chainId] = tierLevel
            val newStars = starCurrency.value + starReward
            
            val json = JSONObject()
            currentTiers.forEach { (k, v) -> json.put(k, v) }

            prefs.edit().apply {
                putString("claimed_chain_tiers_json", json.toString())
                putInt("star_currency", newStars)
            }.apply()

            _claimedChainTiers.value = currentTiers
            _starCurrency.value = newStars
            return true
        }
        return false
    }

    private val _lastGlitchSeedDate = MutableStateFlow(
        prefs.getString("last_glitch_seed_date", "") ?: ""
    )
    val lastGlitchSeedDate = _lastGlitchSeedDate.asStateFlow()

    fun consumeGlitchTicket(todaySeedDate: String) {
        prefs.edit().putString("last_glitch_seed_date", todaySeedDate).apply()
        _lastGlitchSeedDate.value = todaySeedDate
    }

    fun markFtueCompleted() {
        prefs.edit().putBoolean("ftue_completed", true).apply()
        _isFtueCompleted.value = true
    }

    fun recordGameResult(score: Int, combo: Int, starsEarned: Int) {
        val currentBest = prefs.getInt("high_score", 0)
        if (score > currentBest) {
            prefs.edit().putInt("high_score", score).apply()
            _highScore.value = score
        }

        val newRuns = _totalRuns.value + 1
        _totalRuns.value = newRuns

        if (combo > _maxCombo.value) {
            _maxCombo.value = combo
        }

        val newStars = starCurrency.value + starsEarned
        _starCurrency.value = newStars

        prefs.edit().apply {
            putInt("total_runs", newRuns)
            putInt("max_combo", _maxCombo.value)
            putInt("star_currency", newStars)
        }.apply()
    }

    fun recordLinesCleared(lines: Int) {
        if (lines <= 0) return
        val newTotal = _totalLinesCleared.value + lines
        prefs.edit().putInt("total_lines_cleared", newTotal).apply()
        _totalLinesCleared.value = newTotal
    }

    fun claimAchievement(id: String, starReward: Int): Boolean {
        val current = _claimedAchievementIds.value.toMutableSet()
        if (!current.contains(id)) {
            current.add(id)
            val newStars = starCurrency.value + starReward
            prefs.edit().apply {
                putStringSet("claimed_achievements", current)
                putInt("star_currency", newStars)
            }.apply()
            _claimedAchievementIds.value = current
            _starCurrency.value = newStars
            return true
        }
        return false
    }

    fun addStarCurrency(amount: Int) {
        val current = starCurrency.value
        val newValue = current + amount
        prefs.edit().putInt("star_currency", newValue).apply()
        _starCurrency.value = newValue
    }

    fun unlockItem(itemId: String, cost: Int): Boolean {
        if (starCurrency.value >= cost) {
            val newStars = starCurrency.value - cost
            val newUnlocked = _unlockedItemIds.value.toMutableSet().apply { add(itemId) }
            
            prefs.edit().apply {
                putInt("star_currency", newStars)
                putStringSet("unlocked_items", newUnlocked)
            }.apply()

            _starCurrency.value = newStars
            _unlockedItemIds.value = newUnlocked
            return true
        }
        return false
    }

    fun equipItem(itemId: String, category: String) {
        if (_unlockedItemIds.value.contains(itemId)) {
            if (category == "BLOCK_SKINS") {
                prefs.edit().putString("equipped_block_skin", itemId).apply()
                _equippedBlockSkinId.value = itemId
            } else if (category == "VOX_PACKS") {
                prefs.edit().putString("equipped_vox_pack", itemId).apply()
                _equippedVoxPackId.value = itemId
            }
        }
    }

    fun unlockTitle(title: String) {
        val current = _unlockedTitles.value.toMutableSet()
        if (current.add(title)) {
            prefs.edit().putStringSet("unlocked_titles", current).apply()
            _unlockedTitles.value = current
        }
    }

    fun setActiveTitle(title: String) {
        if (_unlockedTitles.value.contains(title)) {
            prefs.edit().putString("active_title", title).apply()
            _activeTitle.value = title
        }
    }

    fun unlockBadge(badgeId: String, badgeRes: Int) {
        val current = _unlockedBadgeIds.value.toMutableSet()
        if (current.add(badgeId)) {
            prefs.edit().putStringSet("unlocked_badges", current).apply()
            _unlockedBadgeIds.value = current
            // Optionally auto-equip first badge
            if (_activeBadgeRes.value == 0) {
                setActiveBadge(badgeRes)
            }
        }
    }

    fun setActiveBadge(badgeRes: Int) {
        prefs.edit().putInt("active_badge_res", badgeRes).apply()
        _activeBadgeRes.value = badgeRes
    }

    fun updateRatingPoints(delta: Int) {
        val current = _ratingPoints.value
        val newValue = (current + delta).coerceAtLeast(0)
        prefs.edit().putInt("rating_points", newValue).apply()
        _ratingPoints.value = newValue
    }

    fun recordSectorCleared(sectorId: Int) {
        val current = _highestSectorCleared.value
        if (sectorId > current) {
            prefs.edit().putInt("highest_sector_cleared", sectorId).apply()
            _highestSectorCleared.value = sectorId
        }
    }

    fun addActiveSectorAugment(augmentId: String) {
        val current = _activeSectorAugmentIds.value.toMutableSet()
        if (current.add(augmentId)) {
            prefs.edit().putStringSet("active_sector_augments", current).apply()
            _activeSectorAugmentIds.value = current
        }
    }

    fun clearActiveSectorAugments() {
        prefs.edit().remove("active_sector_augments").apply()
        _activeSectorAugmentIds.value = emptySet()
    }

    fun unlockAndEquipRelicAbility(abilityName: String) {
        val currentUnlocked = prefs.getStringSet("unlocked_relic_abilities", emptySet()) ?: emptySet()
        val newUnlocked = currentUnlocked.toMutableSet().apply { add(abilityName) }
        prefs.edit().apply {
            putStringSet("unlocked_relic_abilities", newUnlocked)
            putString("equipped_relic_ability", abilityName)
        }.apply()
        _equippedRelicAbilityName.value = abilityName
    }

    fun equipRelicAbility(abilityName: String) {
        prefs.edit().putString("equipped_relic_ability", abilityName).apply()
        _equippedRelicAbilityName.value = abilityName
    }
}
