package com.example.gridsurge.features.adventure.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gridsurge.features.adventure.data.AdventureRepository
import com.example.gridsurge.features.adventure.data.AdventureCatalog
import com.example.gridsurge.features.adventure.model.*
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.features.adventure.ui.dialogs.RelicSpec
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.audio.VoxAction
import com.example.gridsurge.meta.PlayerProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SectorProgressRecord(
    val sectorNumber: Int,
    val starsEarned: Int = 0,
    val isBossDefeated: Boolean = false,
    val isRelicClaimed: Boolean = false
)

data class AdventureUiState(
    val baseMapState: AdventureMapUiState,
    val sectorRecords: Map<Int, SectorProgressRecord> = emptyMap(),
    val claimedRelicReward: RelicSpec? = null // For showing the unlock confirmation toast
)

class AdventureViewModel(
    private val repository: AdventureRepository,
    private val profileManager: PlayerProfileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AdventureUiState(
            baseMapState = AdventureMapUiState(
                currentSectorIndex = 1,
                totalStarsCollected = 0,
                activeSector = AdventureCatalog.SECTORS.firstOrNull() ?: SectorSpec(
                    sectorId = 0,
                    codename = "UNKNOWN",
                    subtitle = "NO DATA",
                    primaryColor = Color.Gray,
                    secondaryColor = Color.DarkGray,
                    requiredStarsToUnlock = 0,
                    backgroundDrawableRes = 0,
                    levels = emptyList<LevelNodeSpec>()
                ),
                allSectors = AdventureCatalog.SECTORS,
                progressMap = emptyMap()
            )
        )
    )
    val uiState: StateFlow<AdventureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.progressFlow, repository.getClaimedRelicSectors()) { progressMap, claimedSectors ->
                Pair(progressMap, claimedSectors)
            }.collect { (progressMap, claimedSectors) ->
                val totalStars = progressMap.values.sumOf { it.starsEarned }
                
                _uiState.update { current ->
                    // Derive sector records from progress map & persistent DataStore claims
                    val updatedSectorRecords = AdventureCatalog.SECTORS.associate { sector ->
                        val sectorStars = sector.levels.sumOf { progressMap[it.levelNumber]?.starsEarned ?: 0 }
                        val bossDefeated = progressMap[sector.levels.lastOrNull { it.isBossLevel }?.levelNumber]?.isCompleted ?: false
                        val isClaimed = (sector.sectorId in claimedSectors) || (current.sectorRecords[sector.sectorId]?.isRelicClaimed == true)
                        
                        sector.sectorId to SectorProgressRecord(
                            sectorNumber = sector.sectorId,
                            starsEarned = sectorStars,
                            isBossDefeated = bossDefeated,
                            isRelicClaimed = isClaimed
                        )
                    }

                    current.copy(
                        baseMapState = current.baseMapState.copy(
                            progressMap = progressMap,
                            totalStarsCollected = totalStars
                        ),
                        sectorRecords = updatedSectorRecords
                    )
                }
            }
        }
    }

    fun onLevelCompleted(levelNumber: Int, score: Long, starsEarned: Int, timeSec: Long) {
        viewModelScope.launch {
            repository.recordLevelCompletion(levelNumber, score, starsEarned, timeSec)
            profileManager.addStarCurrency(starsEarned)
        }
    }

    fun getLevelSpec(levelNumber: Int): LevelNodeSpec? {
        return AdventureCatalog.SECTORS
            .flatMap { it.levels }
            .firstOrNull { it.levelNumber == levelNumber }
    }

    fun getNextLevelSpec(currentLevelNumber: Int): LevelNodeSpec? {
        val nextLevelNumber = currentLevelNumber + 1
        return AdventureCatalog.SECTORS
            .flatMap { it.levels }
            .firstOrNull { it.levelNumber == nextLevelNumber }
    }

    fun selectSector(sectorIndex: Int) {
        if (sectorIndex > 3) return // Soft Launch Cap: Sectors 4 & 5 encrypted
        val sector = AdventureCatalog.SECTORS.find { it.sectorId == sectorIndex } ?: return
        _uiState.update { it.copy(baseMapState = it.baseMapState.copy(activeSector = sector, currentSectorIndex = sectorIndex)) }
    }

    fun selectLevel(level: LevelNodeSpec?) {
        _uiState.update { it.copy(baseMapState = it.baseMapState.copy(selectedLevel = level)) }
    }

    /**
     * Claims the completed Relic:
     * 1. Unlocks the Title in PlayerProfileManager.
     * 2. Unlocks the Badge in PlayerProfileManager.
     * 3. Adds Star Bonus.
     * 4. Marks the relic as claimed in repository and sector state.
     * 5. Plays audio confirm / VOX cues.
     */
    fun claimSectorRelic(relic: RelicSpec) {
        viewModelScope.launch {
            // 1. Grant profile rewards
            profileManager.unlockTitle(relic.rewardTitle)
            profileManager.setActiveTitle(relic.rewardTitle)
            profileManager.unlockBadge(relic.id, relic.rewardBadgeRes)
            profileManager.addStarCurrency(relic.rewardStars)
            
            // Unlock & equip this relic ability across all sectors!
            val relicAbility = RelicAbilityType.getRelicForSector(relic.sectorNumber)
            profileManager.unlockAndEquipRelicAbility(relicAbility.name)

            // Persist claim in repository
            repository.recordRelicClaim(relic.sectorNumber)

            // 2. Mark claimed in Adventure State
            _uiState.update { state ->
                val currentSectorRecord = state.sectorRecords[relic.sectorNumber] 
                    ?: SectorProgressRecord(sectorNumber = relic.sectorNumber)
                
                val updatedRecords = state.sectorRecords + (
                    relic.sectorNumber to currentSectorRecord.copy(isRelicClaimed = true)
                )

                state.copy(
                    sectorRecords = updatedRecords,
                    claimedRelicReward = relic
                )
            }

            // 3. Audio Cues
            SfxManager.playSfx(SfxType.MEGA_BLITZ)
            SfxManager.playVox(VoxAction.OVERDRIVE)
        }
    }

    fun dismissRewardClaimDialog() {
        _uiState.update { it.copy(claimedRelicReward = null) }
    }
}
