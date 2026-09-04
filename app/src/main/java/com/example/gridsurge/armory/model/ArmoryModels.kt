package com.example.gridsurge.armory.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.example.gridsurge.R

enum class ArmoryCategory(val title: String) {
    BLOCK_SKINS("BLOCK SKINS"),
    VOX_PACKS("VOX ANNOUNCERS"),
    CHASSIS_FRAMES("CHASSIS FRAMES")
}

@Immutable
data class ArmoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: ArmoryCategory,
    val priceStars: Int,
    @DrawableRes val previewDrawableRes: Int,
    val themeKey: String,
    val isDefaultUnlocked: Boolean = false
)

object ArmoryCatalog {

    val BLOCK_SKINS = listOf(
        ArmoryItem(
            id = "skin_midnight_glass",
            title = "MIDNIGHT GLASS",
            subtitle = "CRYSTALLINE REFRACTION",
            description = "Precision-beveled deep sapphire crystal with specular prismatic reflection lines.",
            category = ArmoryCategory.BLOCK_SKINS,
            priceStars = 0,
            previewDrawableRes = R.drawable.skin_midnight_glass_cyan,
            themeKey = "theme_midnight_glass",
            isDefaultUnlocked = true
        ),
        ArmoryItem(
            id = "skin_cyber_neon",
            title = "CYBER NEON",
            subtitle = "TACTICAL APERTURE",
            description = "Standard-issue military cyan aperture core with integrated microcircuit inlays.",
            category = ArmoryCategory.BLOCK_SKINS,
            priceStars = 1200,
            previewDrawableRes = R.drawable.skin_cyber_void,
            themeKey = "theme_cyber_void"
        ),
        ArmoryItem(
            id = "skin_solar_flare",
            title = "SOLAR FLARE",
            subtitle = "STELLAR RELIC",
            description = "Ancient sunburst relic pulsing with molten stellar energy and radiant solar coronas.",
            category = ArmoryCategory.BLOCK_SKINS,
            priceStars = 3500,
            previewDrawableRes = R.drawable.skin_solar_flare,
            themeKey = "theme_solar_flare"
        ),
        ArmoryItem(
            id = "skin_voidborn",
            title = "VOIDBORN",
            subtitle = "SINGULARITY SHARD",
            description = "Dark matter matrix fragment with event-horizon gravitational distortion.",
            category = ArmoryCategory.BLOCK_SKINS,
            priceStars = 6000,
            previewDrawableRes = R.drawable.skin_voidborn_purple,
            themeKey = "theme_voidborn"
        ),
        ArmoryItem(
            id = "skin_quantum_matrix",
            title = "QUANTUM MATRIX",
            subtitle = "SUBSPACE GRID",
            description = "Textured 3D energy-etched glass housing an active wireframe matrix core.",
            category = ArmoryCategory.BLOCK_SKINS,
            priceStars = 10000,
            previewDrawableRes = R.drawable.skin_quantum_matrix_cyan,
            themeKey = "theme_quantum_matrix"
        ),
        ArmoryItem(
            id = "skin_hypercube_prism",
            title = "HYPERCUBE PRISM",
            subtitle = "HOLOGRAM MATRIX",
            description = "Pristine 3D cyan crystal bevels featuring an inner floating holographic hypercube core.",
            category = ArmoryCategory.BLOCK_SKINS,
            priceStars = 15000,
            previewDrawableRes = R.drawable.skin_hypercube_prism_cyan,
            themeKey = "theme_hypercube_prism"
        )
    )

    val VOX_PACKS = listOf(
        ArmoryItem(
            id = "vox_default",
            title = "STANDARD LINK",
            subtitle = "SQUAD COMMS",
            description = "Clear, high-fidelity tactical communications without synthesizer interference.",
            category = ArmoryCategory.VOX_PACKS,
            priceStars = 0,
            previewDrawableRes = R.drawable.skin_midnight_glass_cyan,
            themeKey = "",
            isDefaultUnlocked = true
        ),
        ArmoryItem(
            id = "vox_nexus_ai",
            title = "NEXUS AI",
            subtitle = "TACTICAL OPERATING SYSTEM",
            description = "Cool, synthesized combat intelligence with precise military confirmation logs.",
            category = ArmoryCategory.VOX_PACKS,
            priceStars = 2000,
            previewDrawableRes = R.drawable.skin_cyber_void,
            themeKey = "cyber"
        ),
        ArmoryItem(
            id = "vox_solar_pilot",
            title = "SOLAR PILOT",
            subtitle = "VANGUARD SQUADRON",
            description = "Energetic, radio-filtered ace interceptor communications.",
            category = ArmoryCategory.VOX_PACKS,
            priceStars = 5000,
            previewDrawableRes = R.drawable.skin_solar_flare,
            themeKey = "solar"
        ),
        ArmoryItem(
            id = "vox_void_oracle",
            title = "VOID ORACLE",
            subtitle = "ANOMALY FREQUENCY",
            description = "Resonant, multi-layered cosmic entity speaking across subspace channels.",
            category = ArmoryCategory.VOX_PACKS,
            priceStars = 12000,
            previewDrawableRes = R.drawable.skin_voidborn_purple,
            themeKey = "void"
        )
    )

    val ALL_ITEMS: List<ArmoryItem> = BLOCK_SKINS + VOX_PACKS

    fun findItemById(id: String): ArmoryItem? = ALL_ITEMS.firstOrNull { it.id == id }
}

@Immutable
data class ArmoryUserState(
    val starsBalance: Int,
    val unlockedItemIds: Set<String>,
    val equippedBlockSkinId: String,
    val equippedVoxPackId: String
)

@Immutable
data class ArmoryUiState(
    val selectedCategory: ArmoryCategory = ArmoryCategory.BLOCK_SKINS,
    val selectedItem: ArmoryItem = ArmoryCatalog.BLOCK_SKINS.first(),
    val userState: ArmoryUserState = ArmoryUserState(
        starsBalance = 0,
        unlockedItemIds = setOf("skin_midnight_glass", "vox_default"),
        equippedBlockSkinId = "skin_midnight_glass",
        equippedVoxPackId = "vox_default"
    ),
    val isAuditioningAudio: Boolean = false,
    val feedbackMessage: String? = null
)
