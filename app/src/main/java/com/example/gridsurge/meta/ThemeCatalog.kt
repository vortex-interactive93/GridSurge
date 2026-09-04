package com.example.gridsurge.meta

import androidx.annotation.DrawableRes
import com.example.gridsurge.R
import com.example.gridsurge.core.Rarity
import com.example.gridsurge.theme.ThemeNormalizer

data class ThemeBundle(
    val id: String,
    val name: String,
    val rarity: Rarity,
    val description: String,
    @DrawableRes val blockSkinRes: Int,
    @DrawableRes val backgroundRes: Int,
    val primaryAccent: Long,
    val secondaryAccent: Long,
    val unlockStarCost: Int = 0,
    val isAdUnlockable: Boolean = false,
    val isIapExclusive: Boolean = false,
    val iapPriceText: String? = null
)

object ThemeCatalog {
    val DEFAULT_THEME_ID = ThemeNormalizer.GLASS

    val THEMES = listOf(
        ThemeBundle(
            id = ThemeNormalizer.GLASS,
            name = "Midnight Glass",
            rarity = Rarity.STANDARD,
            description = "Classic cyan neon with crystalline light refraction.",
            blockSkinRes = R.drawable.skin_midnight_glass_cyan,
            backgroundRes = R.drawable.bg_classic,
            primaryAccent = 0xFF00E5FF,
            secondaryAccent = 0xFF0077C2,
            unlockStarCost = 0
        ),
        ThemeBundle(
            id = ThemeNormalizer.CYBER,
            name = "Cyber Neon",
            rarity = Rarity.EPIC,
            description = "Hardened tactical terminal housing active bio-circuitry.",
            blockSkinRes = R.drawable.skin_cyber_void,
            backgroundRes = R.drawable.bg_sector_neon_grid,
            primaryAccent = 0xFF00E676,
            secondaryAccent = 0xFF004D40,
            unlockStarCost = 1200
        ),
        ThemeBundle(
            id = ThemeNormalizer.SOLAR,
            name = "Solar Flare",
            rarity = Rarity.RADIANT,
            description = "Ancient sunburst relic pulsing with molten stellar energy.",
            blockSkinRes = R.drawable.skin_solar_flare,
            backgroundRes = R.drawable.bg_sector_solar_flare,
            primaryAccent = 0xFFFFB300,
            secondaryAccent = 0xFFFF6D00,
            unlockStarCost = 3500
        ),
        ThemeBundle(
            id = ThemeNormalizer.VOID,
            name = "Voidborn",
            rarity = Rarity.MYTHIC,
            description = "Dark matter crystal prism encased in deep ultraviolet luminescence.",
            blockSkinRes = R.drawable.skin_voidborn_purple,
            backgroundRes = R.drawable.bg_sector_quantum_singularity,
            primaryAccent = 0xFFE040FB,
            secondaryAccent = 0xFF651FFF,
            unlockStarCost = 6000
        ),
        ThemeBundle(
            id = ThemeNormalizer.QUANTUM,
            name = "Quantum Matrix",
            rarity = Rarity.MYTHIC,
            description = "Textured 3D energy-etched glass housing an active wireframe matrix core.",
            blockSkinRes = R.drawable.skin_quantum_matrix_cyan,
            backgroundRes = R.drawable.bg_sector_quantum_singularity,
            primaryAccent = 0xFF00E5FF,
            secondaryAccent = 0xFF00E676,
            unlockStarCost = 10000
        ),
        ThemeBundle(
            id = ThemeNormalizer.HYPERCUBE,
            name = "Hypercube Prism",
            rarity = Rarity.RADIANT,
            description = "Pristine 3D cyan crystal bevels featuring a floating inner holographic hypercube.",
            blockSkinRes = R.drawable.skin_hypercube_prism_cyan,
            backgroundRes = R.drawable.bg_classic,
            primaryAccent = 0xFF00E5FF,
            secondaryAccent = 0xFF00B0FF,
            unlockStarCost = 15000
        )
    )

    fun getThemeById(id: String): ThemeBundle {
        val canonical = ThemeNormalizer.normalize(id)
        return THEMES.firstOrNull { it.id == canonical } ?: THEMES.first()
    }
}
