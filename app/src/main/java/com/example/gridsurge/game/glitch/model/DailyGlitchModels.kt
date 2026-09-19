package com.example.gridsurge.game.glitch.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

enum class GlitchSessionStatus {
    CONTAINMENT_ACTIVE,
    MELTDOWN_WARNING,   // 3-second critical grace warning (Klaxon + heavy camera shake)
    MATRIX_SATURATED,   // Board ran out of space
    PURITY_COLLAPSED,   // Purity reached 0.0
    SESSION_DEBRIEF     // Submitting to Supabase & showing dialog
}

data class GlitchLiveTelemetry(
    val score: Long = 0L,
    val formattedTime: String = "00:00",
    val catalystsPurged: Int = 0,
    val targetPurgeQuota: Int = 35,
    val systemPurity: Float = 1.0f, // 1.0f down to 0.0f
    val status: GlitchSessionStatus = GlitchSessionStatus.CONTAINMENT_ACTIVE,
    val meltdownCountdownSeconds: Float = 3.0f,
    val seedDateKey: String = "2026-09-10",
    val activeWave: Int = 1
) {
    val isCritical: Boolean get() = systemPurity <= 0.30f || status == GlitchSessionStatus.MELTDOWN_WARNING
}

enum class PurityThreatLevel(
    val statusLabel: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val pulseFrequencyHz: Float
) {
    NOMINAL("NOMINAL // STABLE", Color(0xFF00FF66), Color(0xFF003816), 1.0f),
    CAUTION("VOLTAGE ANOMALY", Color(0xFFFFD600), Color(0xFF4A3E00), 2.0f),
    CRITICAL("CONTAINMENT BREACH", Color(0xFFFF0055), Color(0xFF4A0014), 4.5f),
    COLLAPSED("SYSTEM PURGED", Color(0xFFD500F9), Color(0xFF26002E), 0.0f);

    companion object {
        fun fromPurity(purity: Float): PurityThreatLevel = when {
            purity > 0.65f -> NOMINAL
            purity > 0.30f -> CAUTION
            purity > 0.00f -> CRITICAL
            else -> COLLAPSED
        }
    }
}

data class GlitchVisorTelemetry(
    val score: Long = 0L,
    val formattedTime: String = "00:00",
    val catalystsPurged: Int = 0,
    val targetPurgeQuota: Int = 35,
    val systemPurity: Float = 1.0f, // 1.0f down to 0.0f
    val seedDateKey: String = "2026-09-10",
    val activeWave: Int = 1,
    val isCritical: Boolean = false
)

data class OscilloscopePoint(
    var x: Float = 0f,
    var y: Float = 0f
)

class SpectrumAnalyzerState(val pointCount: Int = 32) {
    val points = Array(pointCount) { OscilloscopePoint() }
    var phase: Float = 0f

    fun update(width: Float, height: Float, dt: Float, threatLevel: Float) {
        if (width <= 0f || height <= 0f) return
        phase += dt * (2.5f + threatLevel * 4.0f)
        val centerY = height / 2f
        val stepX = width / (pointCount - 1)

        for (i in 0 until pointCount) {
            val p = points[i]
            p.x = i * stepX
            val normalizedI = i.toFloat() / pointCount
            val wave1 = sin(normalizedI * 6.283f * 2f + phase)
            val wave2 = sin(normalizedI * 6.283f * 4f - phase * 1.5f)
            val amplitude = (height * 0.35f) * (0.4f + threatLevel * 0.6f)
            p.y = centerY + (wave1 * 0.7f + wave2 * 0.3f).toFloat() * amplitude
        }
    }
}

data class GlitchVisorUiState(
    val score: Long = 0L,
    val timeElapsedFormatted: String = "00:17",
    val catalystsPurged: Int = 0,
    val targetPurgeQuota: Int = 35,
    val systemPurity: Float = 1.0f, // 1.0f down to 0.0f
    val dailySeedKey: String = "SEED // 2026-09-09",
    val activeThreatWave: Int = 1
)

enum class GlitchMatchPhase {
    CONTAINMENT_ACTIVE,
    PURITY_COMPROMISED,
    MATRIX_SATURATED,
    SYNCING_SCORE,
    SESSION_COMPLETE
}

data class GlitchRuptureEvent(
    val cellIndex: Int,
    val penaltyPurity: Float = 0.12f,
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyGlitchDebriefState(
    val dateKey: String,
    val finalScore: Long,
    val catalystsPurged: Int,
    val wavesCompleted: Int,
    val globalRank: Int?,
    val percentileTier: String,
    val starsAwarded: Int,
    val isNewPersonalBest: Boolean = false
)

enum class CatalystStatus(val initialTurns: Int, val diodeColor: Long) {
    STABLE(3, 0xFF00FF66),
    UNSTABLE(2, 0xFFFFD600),
    CRITICAL(1, 0xFFFF1744),
    RUPTURED(0, 0xFFD500F9)
}

data class ActiveAnomalyNode(
    val gridIndex: Int, // 0..63
    var status: CatalystStatus = CatalystStatus.STABLE,
    var turnsRemaining: Int = 3,
    var isRuptured: Boolean = false
)

enum class AnomalyCellPhase(val turns: Int, val color: Color) {
    DORMANT(3, Color(0xFF00FF66)),
    UNSTABLE(2, Color(0xFFFFD600)),
    CRITICAL(1, Color(0xFFFF1744)),
    RUPTURED(0, Color(0xFFD500F9))
}

data class GlitchCatalyst(
    val index: Int, // 0..63
    var phase: AnomalyCellPhase = AnomalyCellPhase.DORMANT,
    var turnsRemaining: Int = 3,
    var isPulsing: Boolean = false,
    var isSlag: Boolean = false
)

data class DailySeedMetadata(
    val dateKey: String, // "YYYY-MM-DD" UTC
    val seedNumeric: Long,
    val resetTimeRemainingMs: Long
) {
    companion object {
        fun currentUtc(): DailySeedMetadata {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dateKey = formatter.format(calendar.time)
            val seed = dateKey.hashCode().toLong()

            // Calculate milliseconds until next 00:00:00 UTC
            val midnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diff = midnight.timeInMillis - calendar.timeInMillis

            return DailySeedMetadata(dateKey, seed, diff)
        }
    }
}

data class GlitchLeaderboardEntry(
    val rank: Int,
    val callsign: String,
    val score: Long,
    val catalystsPurged: Int,
    val finalPurity: Float,
    val timestamp: Long
)

data class DailyGlitchState(
    val seed: DailySeedMetadata,
    val purityIntegrity: Float = 1.0f, // 1.0 down to 0.0 (Collapse)
    val totalCatalystsPurged: Int = 0,
    val targetPurgeQuota: Int = 35,
    val score: Long = 0L,
    val movesPlayed: Int = 0,
    val isGlitchStormActive: Boolean = false,
    val activeInfections: Map<Int, GlitchCatalyst> = emptyMap()
)
