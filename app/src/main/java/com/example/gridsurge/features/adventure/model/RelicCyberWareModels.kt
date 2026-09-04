package com.example.gridsurge.features.adventure.model

import androidx.annotation.DrawableRes
import com.example.gridsurge.R

enum class RelicAbilityType(
    val title: String,
    val calloutText: String,
    val requiredEnergyPoints: Int,
    val unlockedAfterSector: Int,
    val colorHex: Long,
    @DrawableRes val iconRes: Int
) {
    NONE(
        title = "NO RELIC EQUIPPED",
        calloutText = "RELIC LOCKED",
        requiredEnergyPoints = 100,
        unlockedAfterSector = 0,
        colorHex = 0xFF5C8599,
        iconRes = R.drawable.ic_aug_buffer_optimizer
    ),
    CHRONO_BURST(
        title = "CHRONO BURST",
        calloutText = "+15s // 3x3 MATRIX CLEANSE",
        requiredEnergyPoints = 100,
        unlockedAfterSector = 1, // Unlocked upon defeating Sector 01 Boss (Stage 9)
        colorHex = 0xFF00E5FF,
        iconRes = R.drawable.hud_nova_core_supercharged
    ),
    SOLAR_CROSS_LASER(
        title = "SOLAR CROSS LASER",
        calloutText = "CARDINAL LASER STRIKE",
        requiredEnergyPoints = 120,
        unlockedAfterSector = 2,
        colorHex = 0xFFFFD600,
        iconRes = R.drawable.skin_catalyst_cross_block
    ),
    WARP_INJECTION(
        title = "WARP INJECTOR",
        calloutText = "QUANTUM WARP INJECTED",
        requiredEnergyPoints = 140,
        unlockedAfterSector = 3,
        colorHex = 0xFFFF0055,
        iconRes = R.drawable.sector_3_block_no_lock
    ),
    SLAG_TRANSMUTATION(
        title = "BIO TRANSMUTATION",
        calloutText = "ALL SLAG TRANSMUTED",
        requiredEnergyPoints = 110,
        unlockedAfterSector = 4,
        colorHex = 0xFF00FF66,
        iconRes = R.drawable.sector_4_block
    ),
    SUPERNOVA_IMPLOSION(
        title = "SUPERNOVA IMPLOSION",
        calloutText = "EVENT HORIZON COLLAPSE",
        requiredEnergyPoints = 160,
        unlockedAfterSector = 5,
        colorHex = 0xFFEA80FC,
        iconRes = R.drawable.sector_5_block
    );

    companion object {
        fun getRelicForSector(sectorId: Int): RelicAbilityType {
            return when (sectorId) {
                1 -> CHRONO_BURST
                2 -> SOLAR_CROSS_LASER
                3 -> WARP_INJECTION
                4 -> SLAG_TRANSMUTATION
                5 -> SUPERNOVA_IMPLOSION
                else -> NONE
            }
        }

        fun getEquippedRelicForSector(currentSectorId: Int, highestSectorCleared: Int): RelicAbilityType {
            return when {
                highestSectorCleared >= 5 -> SUPERNOVA_IMPLOSION
                highestSectorCleared >= 4 -> SLAG_TRANSMUTATION
                highestSectorCleared >= 3 -> WARP_INJECTION
                highestSectorCleared >= 2 -> SOLAR_CROSS_LASER
                highestSectorCleared >= 1 -> CHRONO_BURST
                else -> NONE // Sector 01 starts with NO relic equipped
            }
        }
    }
}

data class RelicCyberWareState(
    val abilityType: RelicAbilityType,
    val currentEnergy: Int = 0,
    val maxEnergy: Int = 100,
    val isUnlocked: Boolean = false,
    val isReady: Boolean = false,
    val isOverclockDanger: Boolean = false,
    val activationCountThisRun: Int = 0
) {
    val chargeProgress: Float
        get() = if (maxEnergy <= 0) 0f else (currentEnergy.toFloat() / maxEnergy.toFloat()).coerceIn(0f, 1f)
}
