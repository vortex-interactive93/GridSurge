package com.example.gridsurge.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"

    // Official Google Test Ad Unit IDs (guaranteed to load in dev/testing!)
    private const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var isInitialized = false
    private var loadedRewardedAd: RewardedAd? = null
    private var loadedInterstitialAd: InterstitialAd? = null

    private var lastInterstitialTimeMs: Long = 0L
    private const val MIN_INTERSTITIAL_GAP_MS = 180_000L // 3 minutes frequency cap

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) {
                isInitialized = true
                Log.d(TAG, "AdMob SDK Initialized successfully!")
                preloadRewardedAd(context)
                preloadInterstitialAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob SDK", e)
        }
    }

    fun preloadRewardedAd(context: Context) {
        if (loadedRewardedAd != null) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    loadedRewardedAd = rewardedAd
                    Log.d(TAG, "Rewarded Ad preloaded successfully!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadedRewardedAd = null
                    Log.w(TAG, "Failed loading Rewarded Ad: ${loadAdError.message}")
                }
            }
        )
    }

    fun preloadInterstitialAd(context: Context) {
        if (loadedInterstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loadedInterstitialAd = interstitialAd
                    Log.d(TAG, "Interstitial Ad preloaded successfully!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadedInterstitialAd = null
                    Log.w(TAG, "Failed loading Interstitial Ad: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad (e.g. for Pity Revive or 2x Stars).
     * If user purchased No-Ads, immediately triggers onRewardEarned without video!
     */
    fun showRewardedAd(
        activity: Activity,
        isNoAdsPurchased: Boolean,
        onRewardEarned: () -> Unit
    ) {
        if (isNoAdsPurchased) {
            Log.d(TAG, "No-Ads License Active: Instant 1-tap claim!")
            onRewardEarned()
            return
        }

        val ad = loadedRewardedAd
        if (ad != null) {
            ad.show(activity) { _ ->
                Log.d(TAG, "Rewarded ad completed successfully!")
                onRewardEarned()
            }
            loadedRewardedAd = null
            preloadRewardedAd(activity)
        } else {
            // Fallback: If ad failed to load, grant reward so player isn't penalized!
            Log.w(TAG, "Ad not ready; granting reward fallback.")
            onRewardEarned()
            preloadRewardedAd(activity)
        }
    }

    /**
     * Shows a Post-Match Interstitial Ad (frequency capped every 3 minutes).
     * Skipped completely if No-Ads License is active!
     */
    fun showInterstitialAd(activity: Activity, isNoAdsPurchased: Boolean) {
        if (isNoAdsPurchased) return

        val now = System.currentTimeMillis()
        if (now - lastInterstitialTimeMs < MIN_INTERSTITIAL_GAP_MS) return

        val ad = loadedInterstitialAd
        if (ad != null) {
            ad.show(activity)
            lastInterstitialTimeMs = now
            loadedInterstitialAd = null
            preloadInterstitialAd(activity)
        } else {
            preloadInterstitialAd(activity)
        }
    }
}
