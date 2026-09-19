package com.example.gridsurge.monetization.model

enum class ReviveAdPhase {
    DIALOG_IDLE,            // Waiting for player decision
    BUFFERING_AD,           // Spinner active; verifying ad load
    AD_PRESENTING,          // Full-screen video active; game engine in stasis
    REWARD_VALIDATED,       // Video completed successfully; trigger board wipe
    REWARD_REJECTED,        // User closed early or ad failed; proceed to Game Over
    ENTITLED_VIP_BYPASS     // User owns No-Ads; instant wipe without ad
}

data class ReviveGateState(
    val phase: ReviveAdPhase = ReviveAdPhase.DIALOG_IDLE,
    val isAdPrewarmed: Boolean = false,
    val errorMessage: String? = null
)
