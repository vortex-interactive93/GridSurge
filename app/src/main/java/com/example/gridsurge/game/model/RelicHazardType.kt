package com.example.gridsurge.game.model

import com.example.gridsurge.R

enum class RelicHazardType(
    val id: String,
    val displayName: String,
    val drawableRes: Int,
    val defaultHp: Int,
    val sectorIntroduced: Int
) {
    // Sector 01 Relic Asset: Meltdown Reactor
    CYAN_REACTOR("relic_cyan_reactor", "CYAN OVERDRIVE REACTOR", R.drawable.skin_core_block, 2, 1),
    
    // Sector 02 Relic Asset: Solar Cross-Laser
    AMBER_FURNACE("relic_amber_furnace", "AMBER SOLAR FURNACE", R.drawable.skin_core_block, 3, 2),
    
    // Sector 03 Relic Asset: Event Horizon Gyro
    PURPLE_SINGULARITY("relic_purple_singularity", "PURPLE SINGULARITY GYRO", R.drawable.skin_core_block, 1, 3),
    
    // Sector 04 Relic Asset: Entangled Padlock
    CRIMSON_CIPHER_LOCKED("relic_cipher_locked", "CRIMSON CIPHER (LOCKED)", R.drawable.skin_core_block, 1, 4),
    CRIMSON_CIPHER_EXPOSED("relic_cipher_exposed", "CRIMSON CIPHER (EXPOSED)", R.drawable.skin_core_cracked, 1, 4),
    
    // Sector 04 Hazard Variant: Bio Sludge
    EMERALD_CONDUIT("relic_emerald_conduit", "EMERALD BIO-CONDUIT", R.drawable.skin_infected_block, 2, 4)
}
