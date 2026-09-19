package com.example.gridsurge.armory.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.R
import com.example.gridsurge.armory.data.ArmoryCatalog

enum class ArmoryTab(val title: String) {
    BLOCK_SKINS("BLOCK SKINS"),
    VOX_COMMS("VOX COMMS")
}

// Backward-compatible alias
typealias ArmoryCategory = ArmoryTab

enum class ItemRarity(
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    STANDARD("STANDARD", Color(0xFF8FA3BF), Color(0xFF1F2B40)),
    TACTICAL("TACTICAL", Color(0xFF00E5FF), Color(0xFF004D5A)),
    PROTOTYPE("PROTOTYPE", Color(0xFFFFB300), Color(0xFF5A3E00)),
    ANOMALY("ANOMALY", Color(0xFFD500F9), Color(0xFF4A0057))
}

sealed class ArmoryItem(
    open val id: String,
    open val name: String,
    open val callsign: String,
    open val description: String,
    open val priceStars: Int,
    open val rarity: ItemRarity
) {
    // Backward compatibility properties
    val title: String get() = name
    val subtitle: String get() = callsign
    val category: ArmoryTab get() = if (this is BlockSkinItem) ArmoryTab.BLOCK_SKINS else ArmoryTab.VOX_COMMS
    val themeKey: String get() = when (id) {
        "cyber_neon", "vox_nexus" -> "cyber"
        "solar_flare", "vox_solar" -> "solar"
        "voidborn", "vox_void" -> "void"
        else -> ""
    }

    data class BlockSkinItem(
        override val id: String,
        override val name: String,
        override val callsign: String,
        override val description: String,
        override val priceStars: Int,
        override val rarity: ItemRarity,
        @DrawableRes val skinRes: Int,
        val glowColor: Color
    ) : ArmoryItem(id, name, callsign, description, priceStars, rarity)

    data class VoxAnnouncerItem(
        override val id: String,
        override val name: String,
        override val callsign: String,
        override val description: String,
        override val priceStars: Int,
        override val rarity: ItemRarity,
        @RawRes val previewAudioRes: Int,
        val frequencyBand: String
    ) : ArmoryItem(id, name, callsign, description, priceStars, rarity)
}

data class ArmoryUserState(
    val starsBalance: Int = 0,
    val unlockedItemIds: Set<String> = setOf("default_midnight", "vox_standard"),
    val equippedBlockSkinId: String = "default_midnight",
    val equippedVoxPackId: String = "vox_standard"
)

data class ArmoryUiState(
    val activeTab: ArmoryTab = ArmoryTab.BLOCK_SKINS,
    val selectedItem: ArmoryItem = ArmoryCatalog.BLOCK_SKINS.first(),
    val unlockedSkinIds: Set<String> = setOf("default_midnight"),
    val equippedSkinId: String = "default_midnight",
    val unlockedVoxIds: Set<String> = setOf("vox_standard"),
    val equippedVoxId: String = "vox_standard",
    val currentStars: Long = 150L,
    val isAuditioningAudio: Boolean = false,
    val userState: ArmoryUserState = ArmoryUserState(),
    val feedbackMessage: String? = null
) {
    val selectedCategory: ArmoryTab get() = activeTab
}
