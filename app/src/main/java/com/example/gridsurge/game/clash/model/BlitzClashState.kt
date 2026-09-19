package com.example.gridsurge.game.clash.model

enum class ClashDuelStatus {
    COUNTDOWN,
    ACTIVE_COMBAT,
    SUDDEN_DEATH,       // Score delta within 1,000 pts with < 15s remaining
    MATCH_OVER
}

enum class LeadShiftEvent {
    NONE,
    LEAD_GAINED,        // Transitioned from negative to positive delta
    LEAD_LOST           // Transitioned from positive to negative delta
}

data class RivalCombatant(
    val callsign: String = "PHANTOM_X",
    val tierTitle: String = "DIAMOND II",
    val currentMmr: Int = 1840,
    val nextTierMmr: Int = 2000,
    val prevTierMmr: Int = 1700,
    val targetScore: Long = 39200L,
    val avgPpm: Int = 34,
    val avatarKey: String = "avatar_asian_female",
    val equippedBadgeIds: List<String> = listOf("DECA_SURGE", "GRID_NULLIFIER", "FOUNDER")
)

data class ClashArenaSnapshot(
    val playerScore: Long = 0L,
    val rivalScore: Long = 0L,
    val scoreDelta: Long = 0L,              // Positive = Player leads, Negative = Behind
    val secondsRemaining: Float = 90.0f,
    val playerFeverActive: Boolean = false,
    val rivalFeverActive: Boolean = false,
    val playerPpm: Int = 0,
    val rivalPpm: Int = 0,
    val tugOfWarRatio: Float = 0.5f,        // 0.0 (Rival dominating) to 1.0 (Player dominating)
    val duelStatus: ClashDuelStatus = ClashDuelStatus.ACTIVE_COMBAT,
    val lastLeadEvent: LeadShiftEvent = LeadShiftEvent.NONE
)

data class ClashMmrSettlement(
    val isVictory: Boolean,
    val startMmr: Int,
    val mmrDelta: Int,
    val finalMmr: Int,
    val prevTierMmr: Int,
    val nextTierMmr: Int,
    val currentTierName: String,
    val nextTierName: String,
    val finalPlayerScore: Long,
    val finalRivalScore: Long,
    val starsBounty: Int,
    val rival: RivalCombatant
)
