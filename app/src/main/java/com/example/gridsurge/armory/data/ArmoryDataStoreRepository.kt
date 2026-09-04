package com.example.gridsurge.armory.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.gridsurge.armory.model.ArmoryItem
import com.example.gridsurge.armory.model.ArmoryUserState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.armoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "gridsurge_armory_prefs")

class ArmoryDataStoreRepository(private val context: Context) {

    private object PreferencesKeys {
        val EQUIPPED_BLOCK_SKIN = stringPreferencesKey("equipped_block_skin")
        val EQUIPPED_VOX_PACK = stringPreferencesKey("equipped_vox_pack")
        val UNLOCKED_ITEM_IDS = stringSetPreferencesKey("unlocked_item_ids")
    }

    val userStateFlow: Flow<ArmoryUserState> = context.armoryDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val defaultUnlocked = setOf("glass", "vox_default")
            val unlockedIds = prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] ?: defaultUnlocked
            val skinId = prefs[PreferencesKeys.EQUIPPED_BLOCK_SKIN] ?: "glass"
            val voxId = prefs[PreferencesKeys.EQUIPPED_VOX_PACK] ?: "vox_default"

            ArmoryUserState(
                starsBalance = 0, // Balance will be provided by ViewModel from profileManager
                unlockedItemIds = unlockedIds + defaultUnlocked,
                equippedBlockSkinId = skinId,
                equippedVoxPackId = voxId
            )
        }

    suspend fun unlockAndEquipItem(item: ArmoryItem): Result<Unit> {
        context.armoryDataStore.edit { prefs ->
            val unlockedIds = (prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] ?: emptySet()).toMutableSet()

            if (!unlockedIds.contains(item.id)) {
                unlockedIds.add(item.id)
                prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] = unlockedIds
            }
            equipDirect(prefs, item)
        }
        return Result.success(Unit)
    }

    /**
     * Persistently unlocks a new block skin.
     */
    suspend fun unlockBlockSkin(skinId: String) {
        context.armoryDataStore.edit { prefs ->
            val unlockedIds = (prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] ?: emptySet()).toMutableSet()
            unlockedIds.add(skinId)
            prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] = unlockedIds
        }
    }

    /**
     * Unlocks and immediately equips the skin.
     */
    suspend fun unlockAndEquipBlockSkin(skinId: String) {
        context.armoryDataStore.edit { prefs ->
            val unlockedIds = (prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] ?: emptySet()).toMutableSet()
            unlockedIds.add(skinId)
            prefs[PreferencesKeys.UNLOCKED_ITEM_IDS] = unlockedIds
            prefs[PreferencesKeys.EQUIPPED_BLOCK_SKIN] = skinId
        }
    }

    suspend fun equipItem(item: ArmoryItem) {
        context.armoryDataStore.edit { prefs ->
            equipDirect(prefs, item)
        }
    }

    private fun equipDirect(prefs: MutablePreferences, item: ArmoryItem) {
        when (item.category) {
            com.example.gridsurge.armory.model.ArmoryCategory.BLOCK_SKINS -> {
                prefs[PreferencesKeys.EQUIPPED_BLOCK_SKIN] = item.id
            }
            com.example.gridsurge.armory.model.ArmoryCategory.VOX_PACKS -> {
                prefs[PreferencesKeys.EQUIPPED_VOX_PACK] = item.id
            }
            else -> {}
        }
    }
}
