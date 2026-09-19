package com.example.gridsurge.armory.data

import androidx.compose.ui.graphics.Color
import com.example.gridsurge.R
import com.example.gridsurge.armory.model.ArmoryItem
import com.example.gridsurge.armory.model.ItemRarity

object ArmoryCatalog {
    val BLOCK_SKINS = listOf(
        ArmoryItem.BlockSkinItem(
            id = "default_midnight",
            name = "MIDNIGHT GLASS",
            callsign = "CRYSTALLINE REFRACTION",
            description = "Tempered obsidian substrate with high-refraction dielectric edge glow.",
            priceStars = 0,
            rarity = ItemRarity.STANDARD,
            skinRes = R.drawable.skin_midnight_glass_cyan,
            glowColor = Color(0xFF00E5FF)
        ),
        ArmoryItem.BlockSkinItem(
            id = "cyber_neon",
            name = "CYBER NEON",
            callsign = "TACTICAL APERTURE",
            description = "Overclocked phosphor sub-pixels encased in industrial polycarbonate.",
            priceStars = 1200,
            rarity = ItemRarity.TACTICAL,
            skinRes = R.drawable.skin_cyber_void,
            glowColor = Color(0xFF00FF66)
        ),
        ArmoryItem.BlockSkinItem(
            id = "solar_flare",
            name = "SOLAR FLARE",
            callsign = "STELLAR RELIC",
            description = "Superheated plasma matrix with coronal discharge perimeter fields.",
            priceStars = 3500,
            rarity = ItemRarity.PROTOTYPE,
            skinRes = R.drawable.skin_solar_flare,
            glowColor = Color(0xFFFFB300)
        ),
        ArmoryItem.BlockSkinItem(
            id = "voidborn",
            name = "VOIDBORN",
            callsign = "SINGULARITY SHARD",
            description = "Dark matter matrix fragment with event-horizon gravitational distortion.",
            priceStars = 6000,
            rarity = ItemRarity.ANOMALY,
            skinRes = R.drawable.skin_voidborn_purple,
            glowColor = Color(0xFFD500F9)
        ),
        ArmoryItem.BlockSkinItem(
            id = "quantum_matrix",
            name = "QUANTUM MATRIX",
            callsign = "SUBSPACE GRID",
            description = "Non-local polyomino lattice bound by persistent quantum entanglement.",
            priceStars = 10000,
            rarity = ItemRarity.TACTICAL,
            skinRes = R.drawable.skin_quantum_matrix_cyan,
            glowColor = Color(0xFF00E5FF)
        ),
        ArmoryItem.BlockSkinItem(
            id = "hypercube_prism",
            name = "HYPERCUBE PRISM",
            callsign = "HOLOGRAM MATRIX",
            description = "4-dimensional hypercube projection stabilized in 3-space coordinates.",
            priceStars = 15000,
            rarity = ItemRarity.ANOMALY,
            skinRes = R.drawable.skin_hypercube_prism_cyan,
            glowColor = Color(0xFF00E5FF)
        )
    )

    val VOX_ANNOUNCERS = listOf(
        ArmoryItem.VoxAnnouncerItem(
            id = "vox_standard",
            name = "STANDARD LINK",
            callsign = "SQUAD COMMS",
            description = "Clear, high-fidelity tactical comms with minimal synthesizer interference.",
            priceStars = 0,
            rarity = ItemRarity.STANDARD,
            previewAudioRes = R.raw.sfx_modal_whoosh,
            frequencyBand = "142.800 MHz"
        ),
        ArmoryItem.VoxAnnouncerItem(
            id = "vox_nexus",
            name = "NEXUS AI",
            callsign = "TACTICAL OS",
            description = "Cold, analytical synthetic combat processor prioritizing objective clarity.",
            priceStars = 2000,
            rarity = ItemRarity.TACTICAL,
            previewAudioRes = R.raw.sfx_modal_whoosh,
            frequencyBand = "433.920 MHz"
        ),
        ArmoryItem.VoxAnnouncerItem(
            id = "vox_solar",
            name = "SOLAR PILOT",
            callsign = "VANGUARD SQUAD",
            description = "High-energy orbital assault commander with urgent telemetry alerts.",
            priceStars = 5000,
            rarity = ItemRarity.PROTOTYPE,
            previewAudioRes = R.raw.sfx_modal_whoosh,
            frequencyBand = "868.100 MHz"
        ),
        ArmoryItem.VoxAnnouncerItem(
            id = "vox_void",
            name = "VOID ORACLE",
            callsign = "ANOMALY FREQ",
            description = "Resonant sub-bass vocal frequency broadcast from the singularity core.",
            priceStars = 12000,
            rarity = ItemRarity.ANOMALY,
            previewAudioRes = R.raw.sfx_modal_whoosh,
            frequencyBand = "1.240 GHz"
        )
    )

    val VOX_COMMS = VOX_ANNOUNCERS
    val VOX_PACKS = VOX_ANNOUNCERS
    val ALL_ITEMS: List<ArmoryItem> = BLOCK_SKINS + VOX_ANNOUNCERS

    fun findItemById(id: String): ArmoryItem? = ALL_ITEMS.firstOrNull { it.id == id }
}
