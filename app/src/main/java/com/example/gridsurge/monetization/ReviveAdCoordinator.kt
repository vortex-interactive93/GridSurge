package com.example.gridsurge.monetization

import android.app.Activity
import com.example.gridsurge.ads.AdManager
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType

object ReviveAdCoordinator {

    fun initiateReviveSequence(
        activity: Activity,
        isNoAdsUser: Boolean,
        onFreezeEngine: () -> Unit,
        onUnmuteAudio: () -> Unit,
        onRewardVerified: () -> Unit,
        onReviveFailedOrSkipped: () -> Unit
    ) {
        // 1. VIP Bypass: If purchased No-Ads, grant reward instantly
        if (isNoAdsUser) {
            SfxManager.playSfx(SfxType.LEVEL_COMPLETE, overridePitch = 1.3f)
            onRewardVerified()
            return
        }

        // 2. Freeze game loop and mute all audio BEFORE launching ad
        onFreezeEngine()

        AdManager.showRewardedAd(
            activity = activity,
            isNoAdsPurchased = isNoAdsUser,
            onRewardEarned = {
                onUnmuteAudio()
                onRewardVerified()
            },
            onAdNotReady = {
                onUnmuteAudio()
                onReviveFailedOrSkipped()
            }
        )
    }
}
