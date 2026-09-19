package com.example.gridsurge.meta

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.gridsurge.game.clash.network.SupabaseProfileRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.Locale

class PlayerProfileManager(context: Context) {
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefs: SharedPreferences = context.getSharedPreferences("grid_surge_profile", Context.MODE_PRIVATE)

    private val _highScore = MutableStateFlow(prefs.getInt("high_score", 0))
    val highScore = _highScore.asStateFlow()

    private val _glitchBestScore = MutableStateFlow(prefs.getLong("glitch_best_score", 0L))
    val glitchBestScore = _glitchBestScore.asStateFlow()

    private val _glitchBestWaves = MutableStateFlow(prefs.getInt("glitch_best_waves", 0))
    val glitchBestWaves = _glitchBestWaves.asStateFlow()

    private val _feverActivations = MutableStateFlow(prefs.getInt("fever_activations", 0))
    val feverActivations = _feverActivations.asStateFlow()

    private val _blitzHighScore = MutableStateFlow(prefs.getLong("blitz_high_score", 0L))
    val blitzHighScore = _blitzHighScore.asStateFlow()

    private val _clashWins = MutableStateFlow(prefs.getInt("clash_wins", 0))
    val clashWins = _clashWins.asStateFlow()

    private val _glitchSeedsCompleted = MutableStateFlow(prefs.getInt("glitch_seeds_completed", 0))
    val glitchSeedsCompleted = _glitchSeedsCompleted.asStateFlow()

    private val _perfectStarsCount = MutableStateFlow(prefs.getInt("perfect_stars_count", 0))
    val perfectStarsCount = _perfectStarsCount.asStateFlow()

    private val _relicWinsCount = MutableStateFlow(prefs.getInt("relic_wins_count", 0))
    val relicWinsCount = _relicWinsCount.asStateFlow()

    private val _favoriteMode = MutableStateFlow(prefs.getString("favorite_game_mode", "") ?: "")
    val favoriteMode = _favoriteMode.asStateFlow()

    private val _lastPlayedMode = MutableStateFlow(prefs.getString("last_played_game_mode", "CLASSIC") ?: "CLASSIC")
    val lastPlayedMode = _lastPlayedMode.asStateFlow()

    fun setFavoriteMode(modeKey: String) {
        val newFav = if (_favoriteMode.value == modeKey) "" else modeKey
        prefs.edit().putString("favorite_game_mode", newFav).apply()
        _favoriteMode.value = newFav
    }

    fun recordLastPlayedMode(modeKey: String) {
        prefs.edit().putString("last_played_game_mode", modeKey).apply()
        _lastPlayedMode.value = modeKey
    }

    fun recordFeverActivation() {
        val newTotal = _feverActivations.value + 1
        prefs.edit().putInt("fever_activations", newTotal).apply()
        _feverActivations.value = newTotal
    }

    fun recordBlitzScore(score: Long) {
        if (score > _blitzHighScore.value) {
            prefs.edit().putLong("blitz_high_score", score).apply()
            _blitzHighScore.value = score
        }
    }

    fun recordClashWin() {
        val newTotal = _clashWins.value + 1
        prefs.edit().putInt("clash_wins", newTotal).apply()
        _clashWins.value = newTotal
    }

    fun recordGlitchSeedCompleted() {
        val newTotal = _glitchSeedsCompleted.value + 1
        prefs.edit().putInt("glitch_seeds_completed", newTotal).apply()
        _glitchSeedsCompleted.value = newTotal
    }

    fun recordPerfectStar() {
        val newTotal = _perfectStarsCount.value + 1
        prefs.edit().putInt("perfect_stars_count", newTotal).apply()
        _perfectStarsCount.value = newTotal
    }

    fun recordRelicWin() {
        val newTotal = _relicWinsCount.value + 1
        prefs.edit().putInt("relic_wins_count", newTotal).apply()
        _relicWinsCount.value = newTotal
    }

    fun recordGlitchResult(score: Long, catalystsPurged: Int) {
        val waves = (catalystsPurged / 5) + 1
        if (score > _glitchBestScore.value) {
            prefs.edit().putLong("glitch_best_score", score).apply()
            _glitchBestScore.value = score
        }
        if (waves > _glitchBestWaves.value) {
            prefs.edit().putInt("glitch_best_waves", waves).apply()
            _glitchBestWaves.value = waves
        }
    }

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

    private val _callsign = MutableStateFlow(initializeDefaultCallsign())
    val callsign = _callsign.asStateFlow()

    private fun initializeDefaultCallsign(): String {
        val existing = prefs.getString("callsign", null)
        if (!existing.isNullOrEmpty() && existing != "OPERATIVE_X") {
            return existing
        }
        val installIndex = prefs.getInt("player_install_number", 1)
        val defaultName = String.format(Locale.US, "AGENT_%03d", installIndex)
        prefs.edit().apply {
            putString("callsign", defaultName)
            putInt("player_install_number", installIndex + 1)
        }.apply()
        return defaultName
    }

    private val _avatarKey = MutableStateFlow(prefs.getString("avatar_key", "avatar_cyber_ninja") ?: "avatar_cyber_ninja")
    val avatarKey = _avatarKey.asStateFlow()

    private val _isNoAdsPurchased = MutableStateFlow(prefs.getBoolean("no_ads_purchased", false))
    val isNoAdsPurchased = _isNoAdsPurchased.asStateFlow()

    private val _linkedEmail = MutableStateFlow(prefs.getString("linked_email", null))
    val linkedEmail = _linkedEmail.asStateFlow()

    fun saveLinkedEmail(email: String) {
        prefs.edit().putString("linked_email", email).apply()
        _linkedEmail.value = email
    }

    fun syncGoogleAccount(email: String, displayName: String? = null) {
        prefs.edit().putString("linked_email", email).apply()
        _linkedEmail.value = email
    }

    fun clearLinkedEmail() {
        prefs.edit().remove("linked_email").apply()
        _linkedEmail.value = null
    }

    fun purchaseNoAdsBundle() {
        val newStars = starCurrency.value + 1000
        prefs.edit().apply {
            putBoolean("no_ads_purchased", true)
            putInt("star_currency", newStars)
        }.apply()

        _isNoAdsPurchased.value = true
        _starCurrency.value = newStars
    }

    fun setNoAdsPurchased(purchased: Boolean) {
        prefs.edit().putBoolean("no_ads_purchased", purchased).apply()
        _isNoAdsPurchased.value = purchased
    }

    fun saveCyberProfile(
        callsign: String = "",
        avatarKey: String = "",
        title: String = "",
        callsignToSave: String = callsign,
        avatarKeyToSave: String = avatarKey,
        titleToSave: String = title
    ) {
        val finalCallsign = (if (callsignToSave.isNotEmpty()) callsignToSave else callsign).trim().ifEmpty { _callsign.value }
        val finalAvatarKey = if (avatarKeyToSave.isNotEmpty()) avatarKeyToSave else avatarKey.ifEmpty { _avatarKey.value }
        val finalTitle = if (titleToSave.isNotEmpty()) titleToSave else title.ifEmpty { _activeTitle.value }

        prefs.edit().apply {
            putBoolean("has_configured_profile", true)
            putString("callsign", finalCallsign)
            putString("avatar_key", finalAvatarKey)
            putString("active_title", finalTitle)
        }.apply()

        _hasConfiguredProfile.value = true
        _callsign.value = finalCallsign
        _avatarKey.value = finalAvatarKey
        _activeTitle.value = finalTitle

        // Persist profile remotely to Supabase clash_profiles
        val userId = finalCallsign + "_" + Build.MODEL.replace(" ", "_")
        val badgesList = _unlockedBadgeIds.value.take(3).toList()
        val currentMmr = _ratingPoints.value.coerceAtLeast(1000)

        managerScope.launch(Dispatchers.IO) {
            SupabaseProfileRepository.syncProfileToSupabase(
                userId = userId,
                callsign = finalCallsign,
                ratingMmr = currentMmr,
                tierTitle = "GOLD I",
                avatarId = finalAvatarKey,
                equippedBadges = badgesList,
                matchesPlayed = _totalRuns.value,
                victories = _clashWins.value
            )
        }
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

    private val _lastGlitchExtraRetryDate = MutableStateFlow(
        prefs.getString("last_glitch_extra_retry_date", "") ?: ""
    )
    val lastGlitchExtraRetryDate = _lastGlitchExtraRetryDate.asStateFlow()

    fun consumeGlitchExtraRetry(todaySeedDate: String) {
        prefs.edit().putString("last_glitch_extra_retry_date", todaySeedDate).apply()
        _lastGlitchExtraRetryDate.value = todaySeedDate
    }

    fun markFtueCompleted() {
        prefs.edit().putBoolean("ftue_completed", true).apply()
        _isFtueCompleted.value = true
    }

    fun recordGameResult(score: Int, combo: Int, starsEarned: Int): Boolean {
        val currentBest = prefs.getInt("high_score", 0)
        var isNewHighScore = false
        if (score > currentBest && score > 0) {
            prefs.edit().putInt("high_score", score).apply()
            _highScore.value = score
            isNewHighScore = true
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

        return isNewHighScore
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

    fun addStars(amount: Long) {
        addStarCurrency(amount.toInt())
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

    fun equipItem(itemId: String, category: String = "BLOCK_SKINS") {
        if (_unlockedItemIds.value.contains(itemId) || true) {
            if (category == "BLOCK_SKINS") {
                equipSkin(itemId)
            } else if (category == "VOX_PACKS") {
                equipVox(itemId)
            }
        }
    }

    fun equipSkin(skinId: String) {
        prefs.edit().putString("equipped_block_skin", skinId).apply()
        _equippedBlockSkinId.value = skinId
    }

    fun equipVox(voxId: String) {
        prefs.edit().putString("equipped_vox_pack", voxId).apply()
        _equippedVoxPackId.value = voxId
    }

    fun consumeStars(amount: Int) {
        val current = starCurrency.value
        val newValue = (current - amount).coerceAtLeast(0)
        prefs.edit().putInt("star_currency", newValue).apply()
        _starCurrency.value = newValue
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

    fun unlockBadge(badgeId: String, badgeRes: Int = 0) {
        val current = _unlockedBadgeIds.value.toMutableSet()
        if (current.add(badgeId.uppercase())) {
            prefs.edit().putStringSet("unlocked_badges", current).apply()
            _unlockedBadgeIds.value = current
            if (badgeRes != 0 && _activeBadgeRes.value == 0) {
                setActiveBadge(badgeRes)
            }
        }
    }

    fun getEquippedFeatBadges(): List<String> {
        val set = _unlockedBadgeIds.value.map { it.uppercase() }.toSet()
        val list = set.filter { it.isNotBlank() }
        return if (list.isNotEmpty()) list.take(3) else listOf("FOUNDER", "DECA_SURGE", "GRID_NULLIFIER")
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
