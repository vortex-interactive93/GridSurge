package com.example.gridsurge.meta.model

sealed interface IdentityRegistrationState {
    data class DefaultGuest(val autoCallsign: String) : IdentityRegistrationState

    data class PromptPending(
        val suggestedCallsign: String,
        val reason: ClaimTriggerReason
    ) : IdentityRegistrationState

    data class VerifiedCallsign(val callsign: String, val isGoogleLinked: Boolean) : IdentityRegistrationState
}

enum class ClaimTriggerReason {
    FIRST_HIGH_SCORE,
    LEADERBOARD_VIEW,
    GOOGLE_ACCOUNT_LINKED,
    MANUAL_EDIT
}
