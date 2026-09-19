package com.example.gridsurge.monetization.model

enum class AdEligibilityResult {
    ELIGIBLE,
    ON_COOLDOWN,
    INSUFFICIENT_MOVES,
    COMBO_ACTIVE,
    PIECE_HELD,
    SUPPRESSED_BY_REWARDED,
    ENTITLED_VIP_NO_ADS
}

data class AdPacingMetrics(
    val lastAdTimestampMs: Long = 0L,
    val lastRewardedTimestampMs: Long = 0L,
    val piecesPlacedSinceLastAd: Int = 0,
    val totalMatchesPlayed: Int = 0
)

data class AdPacingThresholds(
    val minTimeBetweenInterstitialsMs: Long = 180_000L, // 3 minutes
    val minPiecesBetweenInterstitials: Int = 30,         // ~10 full dock cycles
    val rewardedSuppressionWindowMs: Long = 300_000L,   // 5 minutes
    val minSessionStartDelayMs: Long = 120_000L         // 2 minutes grace on app open
)
