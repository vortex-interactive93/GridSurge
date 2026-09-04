package com.example.gridsurge.features.adventure.domain.model

data class SectorMetadata(
    val sectorId: Int,
    val sectorName: String,
    val subtitle: String,
    val visualThemeKey: String,
    val totalStages: Int = 9,
    val rewardTitle: String,
    val rewardStarGrant: Int,
    val baseDifficultyRating: Int = 1
)
