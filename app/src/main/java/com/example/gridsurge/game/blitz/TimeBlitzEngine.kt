package com.example.gridsurge.game.blitz

enum class BlitzState {
    READY,
    RUNNING,
    FEVER_ACTIVE,
    TIME_EXPIRED
}

data class BlitzMoveResult(
    val scoreDelta: Long,
    val isFeverTriggered: Boolean,
    val timeRefundSec: Float
)

class TimeBlitzEngine(
    val initialTimeSec: Float = 90f
) {
    var state: BlitzState = BlitzState.READY
        private set

    var blitzScore: Long = 0L
        private set

    var secondsRemaining: Float = initialTimeSec
        private set

    var feverMeter: Float = 0f
        private set

    val isFeverActive: Boolean
        get() = state == BlitzState.FEVER_ACTIVE

    private var feverRemainingMs: Long = 0L
    private val feverDurationMs: Long = 10_000L // 10s of 2x Overdrive

    fun startBlitz(nowMs: Long = 0L) {
        state = BlitzState.RUNNING
        secondsRemaining = initialTimeSec
        blitzScore = 0L
        feverMeter = 0f
        feverRemainingMs = 0L
    }

    fun updateFrame(dtSec: Float): BlitzState {
        if (state == BlitzState.TIME_EXPIRED || state == BlitzState.READY) return state

        // 1. Strict countdown timer
        secondsRemaining = (secondsRemaining - dtSec).coerceAtLeast(0f)
        if (secondsRemaining <= 0f) {
            state = BlitzState.TIME_EXPIRED
            return state
        }

        // 2. Timed Fever decay
        if (state == BlitzState.FEVER_ACTIVE) {
            feverRemainingMs = (feverRemainingMs - (dtSec * 1000f).toLong()).coerceAtLeast(0L)
            feverMeter = feverRemainingMs.toFloat() / feverDurationMs.toFloat()

            if (feverRemainingMs <= 0L) {
                state = BlitzState.RUNNING
                feverMeter = 0f
            }
        } else {
            feverMeter = (feverMeter - dtSec * 0.02f).coerceAtLeast(0f)
        }

        return state
    }

    fun onMoveResolved(linesCleared: Int, comboStreak: Int): BlitzMoveResult {
        if (state == BlitzState.TIME_EXPIRED) return BlitzMoveResult(0L, false, 0f)

        val basePoints = when (linesCleared) {
            1 -> 100L
            2 -> 300L
            3 -> 700L
            4 -> 1500L
            else -> 0L
        }

        val multiplier = if (isFeverActive) 2.0f else 1.0f
        val comboMultiplier = 1.0f + (comboStreak * 0.25f)
        val scoreDelta = (basePoints * multiplier * comboMultiplier).toLong()
        blitzScore += scoreDelta

        // Time refund on multi-line clears
        val timeRefund = when {
            linesCleared >= 4 -> 3.0f
            linesCleared >= 3 -> 2.0f
            linesCleared >= 2 -> 1.0f
            else -> 0.0f
        }
        secondsRemaining = (secondsRemaining + timeRefund).coerceAtMost(initialTimeSec)

        var triggeredFever = false
        if (state != BlitzState.FEVER_ACTIVE) {
            val feverGain = (linesCleared * 0.20f) + (comboStreak * 0.05f)
            feverMeter = (feverMeter + feverGain).coerceIn(0f, 1f)

            if (feverMeter >= 1.0f) {
                state = BlitzState.FEVER_ACTIVE
                feverRemainingMs = feverDurationMs
                triggeredFever = true
            }
        }

        return BlitzMoveResult(scoreDelta, triggeredFever, timeRefund)
    }

    fun reset() {
        startBlitz()
    }
}
