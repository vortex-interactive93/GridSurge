package com.example.gridsurge.game.campaign.model

import androidx.compose.ui.graphics.Color

enum class StageNodeStatus {
    LOCKED,
    CURRENT_ACTIVE,     // Next frontline battle
    CLEARED_ONE_STAR,
    CLEARED_TWO_STAR,
    MASTERED_THREE_STAR
}

enum class StageHazardType(val label: String, val badgeColor: Color) {
    NONE("STANDARD PURGE", Color(0xFF00FF66)),
    OBSIDIAN_SLAG("VOLCANIC SLAG", Color(0xFF8A2BE2)),
    REPLICATING_GLITCH("VIRAL MUTATION", Color(0xFFFF0055)),
    CHRONO_LEAK("CHRONO INSTABILITY", Color(0xFFFFD600)),
    CORE_ANOMALY_BOSS("SECTOR CORE OVERLORD", Color(0xFFFF1744))
}

data class StarObjective(
    val description: String,
    val isCompleted: Boolean
)

data class StageBlueprintPreview(
    val initialGlitchCoords: List<Pair<Int, Int>> = emptyList(),
    val prefilledBlockCoords: List<Pair<Int, Int>> = emptyList(),
    val gridSize: Int = 8
)

data class CampaignStageNode(
    val stageId: Int,
    val stageCode: String,            // "STAGE 04"
    val stageTitle: String,           // "VOLTAGE BREACH"
    val xNorm: Float,                 // 0.0 to 1.0 horizontal anchor
    val yNorm: Float,                 // Vertical anchor on the scroll track
    val status: StageNodeStatus,
    val hazardType: StageHazardType,
    val starsEarned: Int = 0,
    val highScore: Long = 0L,
    val objectives: List<StarObjective>,
    val blueprint: StageBlueprintPreview
)

data class SectorMilestoneTier(
    val requiredStars: Int,
    val rewardLabel: String,
    val isClaimed: Boolean,
    val isAvailable: Boolean
)

data class CampaignSectorState(
    val sectorNumber: Int = 1,
    val sectorName: String = "NEURAL NET // SECTOR 01",
    val sectorThemeColor: Color = Color(0xFF00E5FF),
    val totalStarsEarned: Int = 22,
    val maxSectorStars: Int = 45,
    val nodes: List<CampaignStageNode>,
    val milestones: List<SectorMilestoneTier>
)
