package com.example.gridsurge.career.model

import androidx.compose.ui.graphics.Color
import com.example.gridsurge.ui.Screen

enum class OperativeClearance(
    val level: Int,
    val title: String,
    val badgeLabel: String,
    val primaryColor: Color
) {
    RECRUIT(1, "RECRUIT INITIATE", "CLEARANCE T1", Color(0xFF8FA3BF)),
    OPERATIVE(2, "TACTICAL OPERATIVE", "CLEARANCE T2", Color(0xFF00E5FF)),
    SPECIALIST(3, "SURGE SPECIALIST", "CLEARANCE T3", Color(0xFF00FF66)),
    VANGUARD(4, "GRID VANGUARD", "CLEARANCE T4", Color(0xFFFFB300)),
    OVERLORD(5, "NEURAL OVERLORD", "APEX CLEARANCE", Color(0xFFFF1744))
}

data class OperativeDossierState(
    val callsign: String = "OPERATIVE",
    val uidTag: String = "GS-001-ALPHA",
    val clearance: OperativeClearance = OperativeClearance.RECRUIT,
    val currentLevel: Int = 1,
    val currentXp: Long = 0L,
    val xpForNextLevel: Long = 500L,
    val lifetimeSorties: Int = 0,
    val apexHighScore: Long = 0L,
    val totalGridClears: Int = 0,
    val maxComboMultiplier: Int = 0,
    val combatEfficiencyGrade: String = "UNRANKED"
)

data class CascadeAnimationState(
    val isHeaderReady: Boolean = false,
    val isIdCardReady: Boolean = false,
    val isTelemetryReady: Boolean = false,
    val isDirectivesReady: Boolean = false
)

enum class DirectiveCategory(val title: String) {
    ALL("ALL"),
    COMBAT("COMBAT"),
    TACTICAL("TACTICAL"),
    MILESTONES("MILESTONES")
}

enum class MilestoneStatus {
    IN_PROGRESS,
    READY_TO_CLAIM,
    CLAIMED
}

data class CareerDirective(
    val id: String,
    val title: String,
    val description: String,
    val category: DirectiveCategory,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardStars: Int,
    val status: MilestoneStatus = MilestoneStatus.IN_PROGRESS
)
