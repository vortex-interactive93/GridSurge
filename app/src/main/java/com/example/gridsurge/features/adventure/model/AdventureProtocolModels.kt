package com.example.gridsurge.features.adventure.model

import androidx.annotation.DrawableRes
import com.example.gridsurge.R

enum class SectorCoreType(
    val sectorId: Int,
    val displayName: String,
    @DrawableRes val defaultDrawableRes: Int,
    @DrawableRes val crackedDrawableRes: Int? = null,
    @DrawableRes val unlockedDrawableRes: Int? = null
) {
    CHRONO_REACTOR_SEC1(
        sectorId = 1,
        displayName = "CHRONO REACTOR",
        defaultDrawableRes = R.drawable.sector_1_block,
        crackedDrawableRes = R.drawable.sector_1_block_cracked
    ),
    SOLAR_CRUCIBLE_SEC2(2, "SOLAR CRUCIBLE", R.drawable.sector_2_block),
    CRIMSON_CIPHER_SEC3(
        3, 
        "CRIMSON CIPHER", 
        defaultDrawableRes = R.drawable.sector_3_block_with_lock,
        unlockedDrawableRes = R.drawable.sector_3_block_no_lock
    ),
    BIO_CONDUIT_SEC4(4, "EMERALD BIO-CONDUIT", R.drawable.sector_4_block),
    VOID_SINGULARITY_SEC5(5, "EVENT HORIZON GYRO", R.drawable.sector_5_block)
}

enum class AugmentRarity(val colorHex: Long) {
    COMMON(0xFF00E5FF),    // Neon Cyan
    RARE(0xFFFFD600),      // Solar Gold
    LEGENDARY(0xFFFF0055)  // Singularity Crimson
}

enum class AugmentType {
    CHRONO_SIPHON,       // +10s clock refund on every 2-line clear
    CARDINAL_OVERCLOCK,  // Line clears fire secondary laser in perpendicular axis
    WARP_INJECTOR,       // Every 4th tray guaranteed to spawn a 1x1 Quantum Warp Singularity
    BUFFER_OPTIMIZER,    // +2 Combo Grace Moves (Buffer pips never decay on 1st miss)
    CAVITY_COMPRESSOR,   // Smart tray weights down small polyominoes by 40%
    MOLTEN_HARVEST,      // Clearing yellow/gold tiles grants +2.5x score multiplier
    KINETIC_BURST,       // Multi-line clears deal damage to all active cores
    CORROSION_SHIELD     // Hazard slag cannot spread to adjacent cells
}

data class NeuralAugment(
    val id: String,
    val type: AugmentType,
    val title: String,
    val description: String,
    val rarity: AugmentRarity,
    @DrawableRes val iconRes: Int,
    val minSectorRequired: Int = 1
)

data class AdventureStageObjective(
    val type: ObjectiveType = ObjectiveType.INFECTED_PURGE,
    val title: String,
    val targetAmount: Int,
    val star3TimeSec: Int,
    val star2TimeSec: Int
)

data class AdventureLevelBlueprint(
    val levelNumber: Int,
    val sectorId: Int,
    val stageName: String,
    val directive: String,
    val objective: AdventureStageObjective,
    val initialCores: List<CorePlacementSpec>,
    val hasSectorHazards: Boolean = false, // Strictly false for standard tutorial/calibration stages
    val hazardIntervalMoves: Int = 0       // 0 = disabled, >0 = moves between hazard actions
)

data class CorePlacementSpec(
    val col: Int,
    val row: Int,
    val coreType: SectorCoreType,
    val maxHits: Int = 2,
    val isLocked: Boolean = false
)
