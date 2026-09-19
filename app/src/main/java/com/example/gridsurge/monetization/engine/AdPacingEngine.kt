package com.example.gridsurge.monetization.engine

import android.os.SystemClock
import com.example.gridsurge.monetization.model.AdPacingRules

class AdPacingEngine(private val rules: AdPacingRules = AdPacingRules()) {

    private val sessionStartTimeMs: Long = SystemClock.elapsedRealtime()
    private var lastInterstitialShownTimeMs: Long = 0L
    private var lastRewardedWatchedTimeMs: Long = 0L
    private var matchesCompletedCount: Int = 0

    fun onMatchCompleted() {
        matchesCompletedCount++
    }

    fun onRewardedAdWatched() {
        lastRewardedWatchedTimeMs = SystemClock.elapsedRealtime()
    }

    /**
     * Evaluates whether an interstitial ad is permitted to trigger.
     */
    fun canShowInterstitial(isNoAdsEntitlementActive: Boolean, isRankedOrPvP: Boolean): Boolean {
        // 1. VIP/No-Ads and Competitive modes are strictly ad-free
        if (isNoAdsEntitlementActive || isRankedOrPvP) return false

        val now = SystemClock.elapsedRealtime()
        val sessionElapsedSec = (now - sessionStartTimeMs) / 1000L

        // 2. Protect early onboarding retention (first 3 minutes)
        if (sessionElapsedSec < rules.minSessionSecondsBeforeFirstAd) return false

        // 3. Minimum match threshold (never show on the 1st match)
        if (matchesCompletedCount < 2) return false

        // 4. Check Rewarded Ad suppression shield
        val timeSinceRewardedSec = (now - lastRewardedWatchedTimeMs) / 1000L
        if (timeSinceRewardedSec < rules.suppressInterstitialAfterRewardedSec) return false

        // 5. Standard frequency pacing interval
        val timeSinceLastInterstitialSec = (now - lastInterstitialShownTimeMs) / 1000L
        return timeSinceLastInterstitialSec >= rules.minIntervalBetweenInterstitialsSec
    }

    fun recordInterstitialShown() {
        lastInterstitialShownTimeMs = SystemClock.elapsedRealtime()
    }
}
