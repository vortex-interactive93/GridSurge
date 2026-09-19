package com.example.gridsurge.monetization.model

enum class AdPlacement(val placementId: String) {
    DAILY_GLITCH_RETRY("rewarded_glitch_retry"),
    DEBRIEF_STAR_DOUBLER("rewarded_star_doubler"),
    ENDLESS_GRID_FLUSH("rewarded_emergency_flush"),
    MATCH_EXIT_INTERSTITIAL("interstitial_match_exit")
}

sealed class AdAvailabilityState {
    object Idle : AdAvailabilityState()
    object Loading : AdAvailabilityState()
    object Ready : AdAvailabilityState()
    data class Displaying(val placement: AdPlacement) : AdAvailabilityState()
    data class Failed(val errorCode: Int, val errorMessage: String) : AdAvailabilityState()
}

data class AdRewardResult(
    val placement: AdPlacement,
    val isRewardGranted: Boolean,
    val bypassedByEntitlement: Boolean
)

data class AdPacingRules(
    val minSessionSecondsBeforeFirstAd: Long = 180L, // 3 minutes buffer
    val minIntervalBetweenInterstitialsSec: Long = 240L, // 4 minutes
    val suppressInterstitialAfterRewardedSec: Long = 300L // 5 minutes grace
)
