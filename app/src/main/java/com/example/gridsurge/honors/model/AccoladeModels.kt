package com.example.gridsurge.honors.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

enum class HonorTier(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color
) {
    CADET("CADET", Color(0xFF8FA3BF), Color(0xFF1E2D44), Color(0x338FA3BF)),
    VANGUARD("VANGUARD", Color(0xFF00E5FF), Color(0xFF00384D), Color(0x6600E5FF)),
    SOLAR_ELITE("SOLAR ELITE", Color(0xFFFFB300), Color(0xFF4A3400), Color(0x80FFB300)),
    OVERLORD("OVERLORD", Color(0xFFFF1744), Color(0xFF4A0010), Color(0x99FF1744)),
    APEX_SINGULARITY("APEX SINGULARITY", Color(0xFFD500F9), Color(0xFF380042), Color(0xB3D500F9))
}

enum class HonorCategory(val label: String) {
    BLOCK_DEMOLITION("BLOCK PURGE MILESTONES"),
    SECTOR_CAMPAIGN("SECTOR DOMINATION"),
    LEADERBOARD_RANK("GLOBAL TELEMETRY RANK"),
    TACTICAL_MASTERY("COMBAT ACCREDITATION")
}

data class OperativeBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: HonorCategory,
    val tier: HonorTier,
    val targetThreshold: Long,
    val currentProgress: Long,
    val isUnlocked: Boolean = false,
    val rewardStars: Long = 0L,
    @DrawableRes val iconRes: Int,
    @DrawableRes val bannerBackgroundRes: Int
)

data class PlayerPrestigeProfile(
    val callsign: String = "AGENT_881",
    val lifetimeBlocksDestroyed: Long = 48250L,
    val lifetimeStarsEarned: Long = 2450L,
    val globalLeaderboardRank: Int = 84,     // Top 100
    val globalPercentile: Float = 0.985f,    // Top 1.5%
    val equippedBadgeId: String = "badge_blocks_50k",
    val equippedBannerId: String = "banner_top_100"
)
