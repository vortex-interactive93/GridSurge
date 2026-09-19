package com.example.gridsurge.monetization.model

import java.util.Locale

enum class JammerStatus {
    OFFLINE,            // 0s remaining; interstitials enabled
    ACTIVE_STABLE,      // > 10m remaining; clean green status
    ACTIVE_EXPIRING,    // < 5m remaining; amber caution status
    MAX_CAPACITY,       // Capped at 60m; cannot stack further
    PERMANENT_VIP       // Permanent No-Ads purchased; infinite buffer
}

data class FirewallJammerState(
    val remainingSeconds: Long = 0L,
    val maxCapSeconds: Long = 3600L,       // 60 minutes cap
    val incrementSeconds: Long = 300L,     // 5 minutes per ad
    val isPermanentNoAds: Boolean = false,
    val isAdBuffering: Boolean = false
) {
    val status: JammerStatus
        get() = when {
            isPermanentNoAds -> JammerStatus.PERMANENT_VIP
            remainingSeconds >= maxCapSeconds -> JammerStatus.MAX_CAPACITY
            remainingSeconds > 600L -> JammerStatus.ACTIVE_STABLE
            remainingSeconds > 0L -> JammerStatus.ACTIVE_EXPIRING
            else -> JammerStatus.OFFLINE
        }

    val progressRatio: Float
        get() = if (isPermanentNoAds) 1.0f else (remainingSeconds.toFloat() / maxCapSeconds.toFloat()).coerceIn(0f, 1f)

    val formattedTime: String
        get() = if (isPermanentNoAds) {
            "PERMANENT"
        } else {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            String.format(Locale.US, "%02d:%02d", m, s)
        }
}
