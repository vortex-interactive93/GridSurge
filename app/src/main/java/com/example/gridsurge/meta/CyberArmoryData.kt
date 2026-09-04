package com.example.gridsurge.meta

import com.example.gridsurge.core.Rarity

enum class CosmeticType { FULL_SET, BLOCK_SKIN, VOX_PACK, BACKGROUND }

data class CosmeticItem(
    val id: String,
    val name: String,
    val type: CosmeticType,
    val rarity: Rarity,
    val description: String,
    val unlockCostStars: Int,
    val isIapOnly: Boolean = false,
    val priceText: String? = null,
    val primaryColor: Long,
    val accentColor: Long
)

object ArmoryCatalog {
    val MIDNIGHT_GLASS = CosmeticItem(
        id = "midnight_glass",
        name = "Midnight Glass",
        type = CosmeticType.FULL_SET,
        rarity = Rarity.STANDARD,
        description = "Default high-contrast crystalline set.",
        unlockCostStars = 0,
        primaryColor = 0xFF00E5FF,
        accentColor = 0xFF1E2230
    )

    val CYBER_VOID = CosmeticItem(
        id = "cyber_void",
        name = "Cyber Void",
        type = CosmeticType.FULL_SET,
        rarity = Rarity.EPIC,
        description = "Deep purple resonance from the grid's edge.",
        unlockCostStars = 150,
        primaryColor = 0xFFBD10E0,
        accentColor = 0xFF0A0C14
    )

    val SOLAR_FLARE = CosmeticItem(
        id = "solar_flare",
        name = "Solar Flare",
        type = CosmeticType.FULL_SET,
        rarity = Rarity.RADIANT,
        description = "Kinetic energy focused into amber light.",
        unlockCostStars = 300,
        primaryColor = 0xFFF5A623,
        accentColor = 0xFF2D1B00
    )

    val VOIDBORN_OBSIDIAN = CosmeticItem(
        id = "voidborn_obsidian",
        name = "Voidborn Obsidian",
        type = CosmeticType.FULL_SET,
        rarity = Rarity.MYTHIC,
        description = "The ultimate chiseled white obsidian core.",
        unlockCostStars = 0,
        isIapOnly = true,
        priceText = "$2.99",
        primaryColor = 0xFFFFFFFF,
        accentColor = 0xFF000000
    )

    val ALL = listOf(MIDNIGHT_GLASS, CYBER_VOID, SOLAR_FLARE, VOIDBORN_OBSIDIAN)
}
