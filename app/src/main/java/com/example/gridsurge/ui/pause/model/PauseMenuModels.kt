package com.example.gridsurge.ui.pause.model

import androidx.compose.ui.graphics.Color

enum class PauseConfirmationIntent {
    NONE,
    CONFIRM_RESTART,
    CONFIRM_ABORT
}

enum class PauseConfirmationState {
    NONE,
    CONFIRM_RESTART,
    CONFIRM_ABORT
}

enum class PauseActionIntent {
    NONE,
    CONFIRM_RESTART,
    CONFIRM_ABORT
}

data class TelemetryPillSpec(
    val label: String,
    val value: String,
    val accentColor: Color
)

data class TelemetryMetric(
    val label: String,
    val value: String,
    val accentColor: Color
)

data class PauseTerminalConfig(
    val modeTitle: String = "CLASSIC SURGE",
    val modeSubtitle: String = "STANDARD MATRIX",
    val directiveText: String = "Clear lines, maintain high combos, and maximize total score.",
    val elapsedSeconds: Int = 69,
    val telemetryMetrics: List<TelemetryPillSpec> = listOf(
        TelemetryPillSpec("SCORE", "15,013", Color.White),
        TelemetryPillSpec("LINES", "9", Color(0xFF00FF66)),
        TelemetryPillSpec("COMBO", "4x", Color(0xFF00E5FF))
    ),
    val isRestartPermitted: Boolean = true,
    val activeConfirmation: PauseConfirmationIntent = PauseConfirmationIntent.NONE
)

data class PauseTerminalState(
    val modeName: String = "CLASSIC SURGE",
    val modeSubtitle: String = "STANDARD MATRIX",
    val directive: String = "Clear lines, maintain high combos, and maximize total score.",
    val elapsedSeconds: Int = 69,
    val metrics: List<TelemetryMetric> = listOf(
        TelemetryMetric("SCORE", "15,013", Color.White),
        TelemetryMetric("LINES", "9", Color(0xFF00FF66)),
        TelemetryMetric("COMBO", "4x", Color(0xFF00E5FF))
    ),
    val isRestartAllowed: Boolean = true,
    val confirmationState: PauseConfirmationState = PauseConfirmationState.NONE
)

data class PauseMissionTelemetry(
    val gameModeTitle: String = "DAILY GLITCH",
    val sectorOrSeedTag: String = "SEED // 2026-09-10",
    val directive: String = "Purge 35 Anomaly Catalysts before System Purity reaches 0%",
    val currentScore: Long = 99880L,
    val timeElapsedFormatted: String = "01:12",
    val primaryMetricLabel: String = "PURITY INTEGRITY",
    val primaryMetricValue: String = "82%",
    val primaryMetricColor: Color = Color(0xFF00FF66),
    val secondaryMetricLabel: String = "CATALYSTS PURGED",
    val secondaryMetricValue: String = "22 / 35",
    val isRestartAvailable: Boolean = true
)
