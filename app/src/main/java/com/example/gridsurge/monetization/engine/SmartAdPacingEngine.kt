package com.example.gridsurge.monetization.engine

import android.os.SystemClock
import com.example.gridsurge.monetization.model.AdEligibilityResult
import com.example.gridsurge.monetization.model.AdPacingMetrics
import com.example.gridsurge.monetization.model.AdPacingThresholds

class SmartAdPacingEngine(
    private val thresholds: AdPacingThresholds = AdPacingThresholds()
) {
    private var sessionStartMs: Long = SystemClock.elapsedRealtime()
    private var metrics = AdPacingMetrics()

    fun recordPiecePlaced() {
        metrics = metrics.copy(piecesPlacedSinceLastAd = metrics.piecesPlacedSinceLastAd + 1)
    }

    fun recordRewardedAdWatched() {
        val now = SystemClock.elapsedRealtime()
        metrics = metrics.copy(
            lastRewardedTimestampMs = now,
            lastAdTimestampMs = now,
            piecesPlacedSinceLastAd = 0
        )
    }

    fun recordInterstitialShown() {
        metrics = metrics.copy(
            lastAdTimestampMs = SystemClock.elapsedRealtime(),
            piecesPlacedSinceLastAd = 0
        )
    }

    fun recordMatchFinished() {
        metrics = metrics.copy(totalMatchesPlayed = metrics.totalMatchesPlayed + 1)
    }

    /**
     * Pure functional evaluation: Can an ad be shown at this exact millisecond?
     */
    fun evaluateEligibility(
        isNoAdsOwned: Boolean,
        isRankedOrPvP: Boolean,
        activeComboStreak: Int,
        isDraggingPiece: Boolean
    ): AdEligibilityResult {
        if (isNoAdsOwned) return AdEligibilityResult.ENTITLED_VIP_NO_ADS
        if (isRankedOrPvP) return AdEligibilityResult.ON_COOLDOWN // 0% interstitials in competitive

        // 1. Kinetic guards (Never interrupt active focus)
        if (isDraggingPiece) return AdEligibilityResult.PIECE_HELD
        if (activeComboStreak > 0) return AdEligibilityResult.COMBO_ACTIVE

        val now = SystemClock.elapsedRealtime()

        // 2. First-session onboarding grace period
        if (now - sessionStartMs < thresholds.minSessionStartDelayMs) {
            return AdEligibilityResult.ON_COOLDOWN
        }

        // 3. Rewarded suppression shield (Reward players for watching opt-in ads)
        if (now - metrics.lastRewardedTimestampMs < thresholds.rewardedSuppressionWindowMs) {
            return AdEligibilityResult.SUPPRESSED_BY_REWARDED
        }

        // 4. Move threshold guard (Ensure active gameplay occurred)
        if (metrics.piecesPlacedSinceLastAd < thresholds.minPiecesBetweenInterstitials) {
            return AdEligibilityResult.INSUFFICIENT_MOVES
        }

        // 5. Standard time cooldown
        if (now - metrics.lastAdTimestampMs < thresholds.minTimeBetweenInterstitialsMs) {
            return AdEligibilityResult.ON_COOLDOWN
        }

        return AdEligibilityResult.ELIGIBLE
    }
}
