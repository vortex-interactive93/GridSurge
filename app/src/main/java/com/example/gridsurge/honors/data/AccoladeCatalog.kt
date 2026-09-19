package com.example.gridsurge.honors.data

import com.example.gridsurge.R
import com.example.gridsurge.honors.model.*

object AccoladeCatalog {
    val OPERATIVE_HONORS = listOf(
        // Block Purge Hierarchy (Bespoke Military Crest Badges)
        OperativeBadge(
            id = "badge_blocks_10k",
            title = "GRID INITIATE",
            subtitle = "Demolish 10,000 total polyomino blocks",
            category = HonorCategory.BLOCK_DEMOLITION,
            tier = HonorTier.VANGUARD,
            targetThreshold = 10_000L,
            currentProgress = 10_000L,
            isUnlocked = true,
            rewardStars = 100L,
            iconRes = R.drawable.ic_rank_crest_bronze,
            bannerBackgroundRes = R.drawable.bg_sector_neon_grid
        ),
        OperativeBadge(
            id = "badge_blocks_50k",
            title = "CIRCUIT CRUSHER",
            subtitle = "Demolish 50,000 total polyomino blocks",
            category = HonorCategory.BLOCK_DEMOLITION,
            tier = HonorTier.SOLAR_ELITE,
            targetThreshold = 50_000L,
            currentProgress = 48_250L,
            isUnlocked = false,
            rewardStars = 250L,
            iconRes = R.drawable.ic_rank_crest_silver,
            bannerBackgroundRes = R.drawable.bg_sector_solar_flare
        ),
        OperativeBadge(
            id = "badge_blocks_100k",
            title = "SECTOR ANNIHILATOR",
            subtitle = "Demolish 100,000 total polyomino blocks",
            category = HonorCategory.BLOCK_DEMOLITION,
            tier = HonorTier.OVERLORD,
            targetThreshold = 100_000L,
            currentProgress = 48_250L,
            isUnlocked = false,
            rewardStars = 500L,
            iconRes = R.drawable.ic_rank_crest_gold,
            bannerBackgroundRes = R.drawable.bg_sector_crimson_breach
        ),
        OperativeBadge(
            id = "badge_blocks_1m",
            title = "QUANTUM DECONSTRUCTOR",
            subtitle = "Demolish 1,000,000 total polyomino blocks",
            category = HonorCategory.BLOCK_DEMOLITION,
            tier = HonorTier.APEX_SINGULARITY,
            targetThreshold = 1_000_000L,
            currentProgress = 48_250L,
            isUnlocked = false,
            rewardStars = 2_500L,
            iconRes = R.drawable.ic_medal_star_gold,
            bannerBackgroundRes = R.drawable.bg_sector_quantum_singularity
        ),

        // Global Leaderboard Standing
        OperativeBadge(
            id = "banner_top_10_percent",
            title = "ELITE TACTICIAN",
            subtitle = "Reach Global Top 10% on Classic Surge Leaderboards",
            category = HonorCategory.LEADERBOARD_RANK,
            tier = HonorTier.VANGUARD,
            targetThreshold = 90L,
            currentProgress = 98L,
            isUnlocked = true,
            rewardStars = 200L,
            iconRes = R.drawable.ic_medal_crest_bronze,
            bannerBackgroundRes = R.drawable.bg_sector_neon_grid
        ),
        OperativeBadge(
            id = "banner_top_1_percent",
            title = "HIGH OVERLORD",
            subtitle = "Reach Global Top 1% on Classic Surge Leaderboards",
            category = HonorCategory.LEADERBOARD_RANK,
            tier = HonorTier.OVERLORD,
            targetThreshold = 99L,
            currentProgress = 98L,
            isUnlocked = false,
            rewardStars = 750L,
            iconRes = R.drawable.ic_medal_crystal_silver,
            bannerBackgroundRes = R.drawable.bg_sector_crimson_breach
        ),
        OperativeBadge(
            id = "banner_top_100",
            title = "APEX REIGN",
            subtitle = "Secure a Top 100 Global Rank during an active season",
            category = HonorCategory.LEADERBOARD_RANK,
            tier = HonorTier.APEX_SINGULARITY,
            targetThreshold = 100L,
            currentProgress = 84L,
            isUnlocked = true,
            rewardStars = 1_500L,
            iconRes = R.drawable.ic_store_overlord,
            bannerBackgroundRes = R.drawable.bg_sector_quantum_singularity
        )
    )
}
