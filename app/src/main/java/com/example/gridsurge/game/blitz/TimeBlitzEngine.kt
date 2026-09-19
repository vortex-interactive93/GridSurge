package com.example.gridsurge.game.blitz

import com.example.gridsurge.game.blitz.model.BlitzEngineState
import com.example.gridsurge.game.blitz.model.BlitzPhase
import com.example.gridsurge.game.blitz.model.FeverExtensionResult
import com.example.gridsurge.game.blitz.model.TimeBlitzState

data class BlitzMoveResult(
    val scoreDelta: Long,
    val isFeverTriggered: Boolean,
    val timeRefundSec: Float
)

class TimeBlitzEngine(val initialTimeSec: Float = 90.0f) {

    var state = TimeBlitzState(secondsRemaining = initialTimeSec, initialTimeSec = initialTimeSec)
        private set

    val secondsRemaining: Float get() = state.secondsRemaining
    val feverMeter: Float get() = state.feverMeter
    val isFeverActive: Boolean get() = state.isFeverActive
    val blitzScore: Long get() = state.score
    val isTimeExpired: Boolean get() = state.phase == BlitzPhase.SESSION_COMPLETE || state.engineState == BlitzEngineState.TIME_EXPIRED

    var onFeverActivated: (() -> Unit)? = null
    var onFeverDeactivated: (() -> Unit)? = null
    var onFeverExtended: ((FeverExtensionResult) -> Unit)? = null
    var onChronoRefundAwarded: ((secondsAwarded: Int) -> Unit)? = null
    var onCriticalChronoEntered: (() -> Unit)? = null
    var onTimeExpired: (() -> Unit)? = null

    private val feverDrainRatePerSec = 0.125f // Base: 1.0 / 8.0s duration

    fun startBlitz() {
        state = TimeBlitzState(secondsRemaining = initialTimeSec, initialTimeSec = initialTimeSec)
    }

    fun update(dt: Float) {
        if (state.engineState == BlitzEngineState.TIME_EXPIRED || state.engineState == BlitzEngineState.IDLE) return

        var newTime = state.secondsRemaining - dt
        var newFever = state.feverMeter
        var isFever = state.isFeverActive
        var uptimeAcc = state.feverUptimeSeconds

        // 1. Accumulate Fever Uptime and Drain Gauge
        if (isFever) {
            uptimeAcc += dt
            newFever = (newFever - (feverDrainRatePerSec * dt)).coerceAtLeast(0f)

            if (newFever <= 0f) {
                isFever = false
                newFever = 0f
                onFeverDeactivated?.invoke()
            }
        }

        // 2. Resolve Global Engine State
        val newPhase = when {
            newTime <= 0f -> BlitzPhase.SESSION_COMPLETE
            isFever -> BlitzPhase.FEVER_OVERDRIVE
            newTime <= 15.0f -> {
                if (state.phase != BlitzPhase.CRITICAL_CHRONO) onCriticalChronoEntered?.invoke()
                BlitzPhase.CRITICAL_CHRONO
            }
            else -> BlitzPhase.ACTIVE_RUN
        }

        val newEngineState = when {
            newTime <= 0f -> BlitzEngineState.TIME_EXPIRED
            isFever -> BlitzEngineState.FEVER_ACTIVE
            newTime <= 15.0f -> BlitzEngineState.CRITICAL_CHRONO
            else -> BlitzEngineState.ACTIVE_RUN
        }

        if (newTime <= 0f) {
            newTime = 0f
            state = state.copy(
                secondsRemaining = 0f,
                feverMeter = 0f,
                isFeverActive = false,
                feverUptimeSeconds = uptimeAcc,
                phase = BlitzPhase.SESSION_COMPLETE,
                engineState = BlitzEngineState.TIME_EXPIRED
            )
            onTimeExpired?.invoke()
            return
        }

        state = state.copy(
            secondsRemaining = newTime,
            feverMeter = newFever,
            isFeverActive = isFever,
            feverUptimeSeconds = uptimeAcc,
            currentMultiplier = if (isFever) 3 else 1,
            phase = newPhase,
            engineState = newEngineState
        )
    }

    fun updateFrame(dtSec: Float): BlitzPhase {
        update(dtSec)
        return state.phase
    }

    /**
     * Line-Clear Evaluator:
     * Handles both initial Fever charge AND active Fever extensions (Overdrive Siphon Rule).
     */
    fun onLinesCleared(lines: Int, currentStreak: Int): Int {
        if (lines <= 0) return 0

        // 1. Chrono Refund for the Match Timer
        var matchSecondsRefund = when (lines) {
            2 -> 2
            3 -> 4
            else -> if (lines >= 4) 7 else 0
        }
        if (currentStreak >= 4) matchSecondsRefund += 1

        val updatedMatchTime = (state.secondsRemaining + matchSecondsRefund).coerceAtMost(99.0f)
        var updatedFever = state.feverMeter
        var feverActivatedThisTurn = false

        // 2. Resolve Fever Meter & Overdrive Extension
        if (state.isFeverActive) {
            // === FEVER EXTENSION LOGIC (OVERDRIVE SIPHON FORMULA) ===
            // 1 line = +25% (+2.0s), 2 lines = +50% (+4.0s), 3+ lines = +75% (+6.0s)
            val extensionCharge = when (lines) {
                1 -> 0.25f + (currentStreak * 0.03f)
                2 -> 0.50f + (currentStreak * 0.05f)
                else -> 0.75f + (currentStreak * 0.06f)
            }

            updatedFever = (updatedFever + extensionCharge).coerceAtMost(1.0f)
            val addedSeconds = extensionCharge / feverDrainRatePerSec

            val tag = if (lines >= 2) "+${addedSeconds.toInt()}s OVERDRIVE SURGE!" else "+${addedSeconds.toInt()}s FEVER EXTEND"
            onFeverExtended?.invoke(
                FeverExtensionResult(
                    secondsAdded = addedSeconds,
                    resultingMeter = updatedFever,
                    comboStreak = currentStreak,
                    displayTag = tag
                )
            )
        } else {
            // === NORMAL CHARGE TO TRIGGER FEVER ===
            val normalCharge = (lines * 0.22f) + (currentStreak * 0.05f)
            updatedFever = (updatedFever + normalCharge).coerceAtMost(1.0f)

            if (updatedFever >= 1.0f) {
                feverActivatedThisTurn = true
                updatedFever = 1.0f
            }
        }

        val basePoints = when (lines) {
            1 -> 100L
            2 -> 300L
            3 -> 700L
            4 -> 1500L
            else -> (lines * 400L)
        }
        val multiplier = if (state.isFeverActive || feverActivatedThisTurn) 3.0f else 1.0f
        val comboMultiplier = 1.0f + (currentStreak * 0.25f)
        val scoreDelta = (basePoints * multiplier * comboMultiplier).toLong()

        if (matchSecondsRefund > 0) {
            onChronoRefundAwarded?.invoke(matchSecondsRefund)
        }

        state = state.copy(
            score = state.score + scoreDelta,
            secondsRemaining = updatedMatchTime,
            feverMeter = updatedFever,
            isFeverActive = state.isFeverActive || feverActivatedThisTurn,
            linesClearedTotal = state.linesClearedTotal + lines,
            maxComboStreak = maxOf(state.maxComboStreak, currentStreak),
            totalTimeRefundedSec = state.totalTimeRefundedSec + matchSecondsRefund,
            currentMultiplier = if (state.isFeverActive || feverActivatedThisTurn) 3 else 1
        )

        if (feverActivatedThisTurn) {
            onFeverActivated?.invoke()
        }

        return matchSecondsRefund
    }

    fun onMoveResolved(linesCleared: Int, comboStreak: Int): BlitzMoveResult {
        val secondsRefund = onLinesCleared(linesCleared, comboStreak)
        val isFever = state.isFeverActive
        return BlitzMoveResult(state.score, isFever, secondsRefund.toFloat())
    }

    fun onPieceCommitted() {
        state = state.copy(piecesPlacedTotal = state.piecesPlacedTotal + 1)
    }

    fun reset() {
        startBlitz()
    }
}
