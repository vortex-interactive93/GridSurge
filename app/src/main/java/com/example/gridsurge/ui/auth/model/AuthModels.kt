package com.example.gridsurge.ui.auth.model

import androidx.annotation.DrawableRes
import com.example.gridsurge.R

enum class CloudSyncState {
    IDLE,
    SYNCING,
    SYNC_SUCCESS,
    SYNC_FAILED
}

enum class SecurityTelemetry(
    val title: String,
    val guestDesc: String,
    val linkedStatus: String,
    @DrawableRes val iconRes: Int
) {
    CLOUD_VAULT(
        title = "CLOUD SAVE",
        guestDesc = "Unlinked",
        linkedStatus = "SYNCED",
        iconRes = R.drawable.ic_hud_cloud_save
    ),
    GLOBAL_RANKS(
        title = "LEADERBOARDS",
        guestDesc = "Locked",
        linkedStatus = "VERIFIED",
        iconRes = R.drawable.ic_hud_leaderboard
    ),
    CROSS_LINK(
        title = "MULTI-DEVICE",
        guestDesc = "Offline",
        linkedStatus = "ONLINE",
        iconRes = R.drawable.ic_hud_cross_device
    )
}
