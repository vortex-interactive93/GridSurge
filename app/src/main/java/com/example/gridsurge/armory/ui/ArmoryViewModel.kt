package com.example.gridsurge.armory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gridsurge.armory.ArmoryEngineBridge
import com.example.gridsurge.armory.model.ArmoryCatalog
import com.example.gridsurge.armory.model.ArmoryCategory
import com.example.gridsurge.armory.model.ArmoryItem
import com.example.gridsurge.armory.model.ArmoryUiState
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.meta.PlayerProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArmoryViewModel(
    private val profileManager: PlayerProfileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArmoryUiState())
    val uiState: StateFlow<ArmoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileManager.starCurrency.collect { stars ->
                _uiState.update { it.copy(userState = it.userState.copy(starsBalance = stars)) }
            }
        }
        viewModelScope.launch {
            profileManager.unlockedItemIds.collect { unlocked ->
                _uiState.update { it.copy(userState = it.userState.copy(unlockedItemIds = unlocked)) }
            }
        }
        viewModelScope.launch {
            profileManager.equippedBlockSkinId.collect { skinId ->
                _uiState.update { it.copy(userState = it.userState.copy(equippedBlockSkinId = skinId)) }
            }
        }
        viewModelScope.launch {
            profileManager.equippedVoxPackId.collect { voxId ->
                _uiState.update { it.copy(userState = it.userState.copy(equippedVoxPackId = voxId)) }
            }
        }
    }

    fun selectCategory(category: ArmoryCategory) {
        if (_uiState.value.selectedCategory == category) return
        SfxManager.playSfx(SfxType.SNAP_TICK)
        val defaultItem = getItemsForCategory(category).first()
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedItem = defaultItem,
                feedbackMessage = null
            )
        }
    }

    fun selectItem(item: ArmoryItem) {
        if (_uiState.value.selectedItem.id == item.id) return
        SfxManager.playSfx(SfxType.SNAP_TICK)
        _uiState.update { it.copy(selectedItem = item, feedbackMessage = null) }

        if (item.category == ArmoryCategory.VOX_PACKS) {
            ArmoryEngineBridge.auditionVoxPack(item.themeKey)
        }
    }

    fun onPrimaryActionClicked() {
        val state = _uiState.value
        val item = state.selectedItem
        val isUnlocked = state.userState.unlockedItemIds.contains(item.id)
        val isEquipped = when (item.category) {
            ArmoryCategory.BLOCK_SKINS -> state.userState.equippedBlockSkinId == item.id
            ArmoryCategory.VOX_PACKS -> state.userState.equippedVoxPackId == item.id
            else -> false
        }

        if (isEquipped) return

        viewModelScope.launch {
            if (isUnlocked) {
                profileManager.equipItem(item.id, item.category.name)
                SfxManager.playSfx(SfxType.UI_CONFIRM)
                _uiState.update { it.copy(feedbackMessage = "SYSTEM EQUIPPED // ${item.title}") }
            } else {
                if (profileManager.starCurrency.value >= item.priceStars) {
                    val success = profileManager.unlockItem(item.id, item.priceStars)
                    if (success) {
                        profileManager.equipItem(item.id, item.category.name)
                        SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE, priority = 4)
                        _uiState.update { it.copy(feedbackMessage = "UNLOCKED & EQUIPPED // ${item.title}") }
                    }
                } else {
                    SfxManager.playSfx(SfxType.INVALID_MOVE)
                    _uiState.update { it.copy(feedbackMessage = "ACCESS DENIED: INSUFFICIENT STARS") }
                }
            }
        }
    }

    fun getItemsForCategory(category: ArmoryCategory): List<ArmoryItem> {
        return when (category) {
            ArmoryCategory.BLOCK_SKINS -> ArmoryCatalog.BLOCK_SKINS
            ArmoryCategory.VOX_PACKS -> ArmoryCatalog.VOX_PACKS
            ArmoryCategory.CHASSIS_FRAMES -> emptyList()
        }
    }
}
