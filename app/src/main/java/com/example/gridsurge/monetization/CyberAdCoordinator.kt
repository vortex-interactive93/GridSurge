package com.example.gridsurge.monetization

import android.app.Activity
import android.content.Context
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.monetization.engine.AdPacingEngine
import com.example.gridsurge.monetization.model.AdAvailabilityState
import com.example.gridsurge.monetization.model.AdPlacement
import com.example.gridsurge.monetization.model.AdRewardResult
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CyberAdCoordinator(
    private val context: Context,
    private val pacingEngine: AdPacingEngine
) {
    private val _rewardedState = MutableStateFlow<AdAvailabilityState>(AdAvailabilityState.Idle)
    val rewardedState: StateFlow<AdAvailabilityState> = _rewardedState.asStateFlow()

    private var activeRewardedAd: RewardedAd? = null
    private var isAdPreloading = false

    // Wire to Google Play Billing Client
    var isNoAdsPurchased: Boolean = false

    /**
     * Preload ads in the background. Call this on Hub Mount or when idle.
     * NEVER call during active gameplay or drag events.
     */
    fun prewarmRewardedAd() {
        if (isNoAdsPurchased || isAdPreloading || activeRewardedAd != null) return
        isAdPreloading = true
        _rewardedState.value = AdAvailabilityState.Loading

        val adRequest = AdRequest.Builder().build()
        // Replace with production AdMob Rewarded Unit ID
        val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test ID

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    activeRewardedAd = ad
                    isAdPreloading = false
                    _rewardedState.value = AdAvailabilityState.Ready
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    activeRewardedAd = null
                    isAdPreloading = false
                    _rewardedState.value = AdAvailabilityState.Failed(error.code, error.message)
                }
            }
        )
    }

    /**
     * Shows a rewarded ad or immediately grants the reward if the player has No-Ads VIP.
     */
    fun showRewardedAd(
        activity: Activity,
        placement: AdPlacement,
        onEnginePause: () -> Unit = {},
        onEngineResume: () -> Unit = {},
        onRewardResolved: (AdRewardResult) -> Unit
    ) {
        // VIP / No-Ads Entitlement Bypass
        if (isNoAdsPurchased) {
            SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.3f)
            onRewardResolved(
                AdRewardResult(
                    placement = placement,
                    isRewardGranted = true,
                    bypassedByEntitlement = true
                )
            )
            return
        }

        val ad = activeRewardedAd
        if (ad == null) {
            // Ad wasn't ready; fail gracefully without locking the player
            onRewardResolved(
                AdRewardResult(
                    placement = placement,
                    isRewardGranted = false,
                    bypassedByEntitlement = false
                )
            )
            prewarmRewardedAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                _rewardedState.value = AdAvailabilityState.Displaying(placement)
                onEnginePause() // Pause 120 FPS game view & stop SoundPool
            }

            override fun onAdDismissedFullScreenContent() {
                activeRewardedAd = null
                _rewardedState.value = AdAvailabilityState.Idle
                pacingEngine.onRewardedAdWatched()
                onEngineResume() // Resume game view & restore audio
                prewarmRewardedAd() // Pre-load next ad
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                activeRewardedAd = null
                _rewardedState.value = AdAvailabilityState.Failed(error.code, error.message)
                onEngineResume()
                prewarmRewardedAd()
                onRewardResolved(
                    AdRewardResult(placement, isRewardGranted = false, bypassedByEntitlement = false)
                )
            }
        }

        // Present full-screen ad
        ad.show(activity) { _ ->
            onRewardResolved(
                AdRewardResult(placement, isRewardGranted = true, bypassedByEntitlement = false)
            )
        }
    }
}
