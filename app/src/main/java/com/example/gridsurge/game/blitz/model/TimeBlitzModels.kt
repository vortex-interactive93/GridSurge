package com.example.gridsurge.game.blitz.model

import androidx.compose.ui.graphics.Color

data class ExhaustParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var alpha: Float = 1f,
    var size: Float = 4f,
    var color: Int = android.graphics.Color.CYAN,
    var active: Boolean = false
)

data class PersonalBestSnapshot(
    val bestPpm: Int = 30,
    val bestCombo: Int = 4,
    val bestScore: Long = 35000L
)

enum class BlitzEngineState {
    IDLE,
    ACTIVE_RUN,
    FEVER_ACTIVE,
    CRITICAL_CHRONO, // Last 15 seconds
    TIME_EXPIRED
}

enum class BlitzTerminalPhase {
    RUNNING,
    FREEZE_STASIS,      // 0.8s freeze with "TIME'S UP!" banner
    BOARD_SWEEP,        // 0.4s laser deactivation sweep
    DEBRIEF_MOUNTED     // Dialog displayed with animated counters
}

data class BlitzTerminalSequenceState(
    var phase: BlitzTerminalPhase = BlitzTerminalPhase.RUNNING,
    var sequenceElapsedSec: Float = 0f,
    val freezeDurationSec: Float = 0.8f,
    val sweepDurationSec: Float = 0.4f
) {
    val totalSequenceDuration: Float get() = freezeDurationSec + sweepDurationSec
    val isSequenceFinished: Boolean get() = phase == BlitzTerminalPhase.DEBRIEF_MOUNTED
}

enum class ChronoRefundTier(val seconds: Int, val tag: String, val color: Color) {
    NONE(0, "", Color.Transparent),
    DOUBLE_CLEAR(2, "+2s CHRONO", Color(0xFF00E5FF)),
    TRIPLE_CLEAR(4, "+4s OVERCLOCK", Color(0xFFFFD600)),
    MEGA_BLITZ(7, "+7s SUPERNOVA", Color(0xFFFF1744))
}

data class BuzzerBeaterState(
    var isHoldingPieceAtZero: Boolean = false,
    var gracePeriodRemainingSec: Float = 0.5f,
    var hasResolvedFinalDrop: Boolean = false
)

data class ScreenVignetteState(
    var intensity: Float = 0.0f, // 0.0 to 1.0
    var pulsePhase: Float = 0.0f
)

data class FeverExtensionResult(
    val secondsAdded: Float,
    val resultingMeter: Float,
    val comboStreak: Int,
    val displayTag: String
)

data class FeverExtensionEvent(
    val secondsAdded: Float,
    val newMeterValue: Float,
    val extensionTier: String
)

enum class BlitzPhase {
    STANDBY,
    ACTIVE_RUN,
    FEVER_OVERDRIVE,
    CRITICAL_CHRONO, // Last 15 seconds
    OVERTIME_CASCADE, // Final board clear bonus
    SESSION_COMPLETE
}

data class TimeBlitzState(
    val score: Long = 0L,
    val secondsRemaining: Float = 90.0f,
    val initialTimeSec: Float = 90.0f,
    val feverMeter: Float = 0.0f, // 0.0 to 1.0
    val isFeverActive: Boolean = false,
    val feverDurationSec: Float = 0.0f,
    val currentMultiplier: Int = 1,
    val linesClearedTotal: Int = 0,
    val piecesPlacedTotal: Int = 0,
    val maxComboStreak: Int = 0,
    val totalTimeRefundedSec: Int = 0,
    val feverUptimeSeconds: Float = 0.0f,
    val phase: BlitzPhase = BlitzPhase.ACTIVE_RUN,
    val engineState: BlitzEngineState = BlitzEngineState.ACTIVE_RUN
) {
    val feverUptimePercent: Int
        get() {
            val totalPlayTimeSec = (initialTimeSec + totalTimeRefundedSec - secondsRemaining).coerceAtLeast(1.0f)
            return ((feverUptimeSeconds / totalPlayTimeSec) * 100f).toInt().coerceIn(0, 100)
        }

    val piecesPerMinute: Int
        get() {
            val elapsedSec = (initialTimeSec + totalTimeRefundedSec - secondsRemaining).coerceAtLeast(1.0f)
            return ((piecesPlacedTotal / elapsedSec) * 60f).toInt()
        }
}

data class BlitzScorecardDebrief(
    val finalScore: Long,
    val linesCleared: Int,
    val maxCombo: Int,
    val piecesPerMinute: Int,
    val feverUptimePercent: Int,
    val chronoRefundTotalSec: Int,
    val starReward: Int,
    val globalRank: Int? = null,
    val isNewPersonalBest: Boolean = false,
    val isNewPpmPb: Boolean = false,
    val isNewComboPb: Boolean = false
)
