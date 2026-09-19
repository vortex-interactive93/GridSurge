package com.example.gridsurge.hub.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.gridsurge.R
import com.example.gridsurge.ui.Screen

enum class GameMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val ctaLabel: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val targetScreen: Screen,
    @DrawableRes val glyphRes: Int
) {
    CLASSIC(
        id = "mode_classic",
        title = "CLASSIC SURGE",
        subtitle = "INFINITE PUZZLE MATRIX",
        badgeLabel = "TACTICAL APERTURE",
        ctaLabel = "▶ DEPLOY SURGE",
        description = "Endless deployment. Clear intersecting clusters, maintain grid stability, and maximize score multipliers.",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF004D5A),
        targetScreen = Screen.GAME_CLASSIC,
        glyphRes = R.drawable.ic_watermark_classic
    ),
    CAMPAIGN(
        id = "mode_campaign",
        title = "SECTOR CAMPAIGN",
        subtitle = "SECTOR 03: OVERLORD",
        badgeLabel = "TACTICAL INCURSION",
        ctaLabel = "▶ INFILTRATE SECTOR",
        description = "Infiltrate neural nodes, purge corrupted sub-routines, and harvest boss cores across 24 sectors.",
        primaryColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFF5A3E00),
        targetScreen = Screen.GAME_ADVENTURE,
        glyphRes = R.drawable.ic_watermark_campaign
    ),
    DAILY_GLITCH(
        id = "mode_daily_glitch",
        title = "DAILY GLITCH",
        subtitle = "CORRUPTED SEED // 24H",
        badgeLabel = "SYNCHRONIZED ANOMALY",
        ctaLabel = "▶ PURGE ANOMALY",
        description = "A globally synchronized daily seed containing erratic corrupted blocks and high-yield star bounties.",
        primaryColor = Color(0xFF00FF66),
        secondaryColor = Color(0xFF00471B),
        targetScreen = Screen.DAILY_GLITCH,
        glyphRes = R.drawable.ic_watermark_glitch
    ),
    TIME_BLITZ(
        id = "mode_time_blitz",
        title = "TIME BLITZ",
        subtitle = "120S VELOCITY SURGE",
        badgeLabel = "OVERDRIVE CLOCK",
        ctaLabel = "▶ ENGAGE BLITZ",
        description = "High-velocity score assault. Chain rapid line wipes to trigger 2.0x Fever Surge before the timer decays.",
        primaryColor = Color(0xFFD500F9),
        secondaryColor = Color(0xFF4A0057),
        targetScreen = Screen.TIME_BLITZ,
        glyphRes = R.drawable.ic_watermark_blitz
    ),
    BLITZ_CLASH(
        id = "mode_blitz_clash",
        title = "BLITZ CLASH",
        subtitle = "1V1 GHOST DUEL",
        badgeLabel = "PVP COMBAT RECON",
        ctaLabel = "▶ DUEL RIVAL",
        description = "75-second asynchronous PvP match against operative ghost replays with EMP jammers.",
        primaryColor = Color(0xFFFF1744),
        secondaryColor = Color(0xFF5A0012),
        targetScreen = Screen.BLITZ_CLASH,
        glyphRes = R.drawable.ic_watermark_clash
    )
}

data class ModeTelemetry(
    val apexScore: Long,
    val secondaryLabel: String,
    val secondaryValue: String,
    val isLocked: Boolean = false,
    val lockRequirement: String? = null
)

data class ModeHubUiState(
    val selectedIndex: Int = 0,
    val agentCallsign: String = "AGENT_881",
    val currentStars: Long = 700L,
    val modeTelemetryMap: Map<GameMode, ModeTelemetry> = emptyMap()
)
