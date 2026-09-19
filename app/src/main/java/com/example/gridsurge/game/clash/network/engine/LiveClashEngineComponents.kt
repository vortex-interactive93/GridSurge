package com.example.gridsurge.game.clash.network.engine

import com.example.gridsurge.game.engine.SeededTrayGenerator
import com.example.gridsurge.game.model.PolyShape
import kotlin.math.min
import kotlin.math.pow

class MatchmakingRangeCalculator(
    private val baseWindowMmr: Int = 60,
    private val maxWindowMmr: Int = 350,
    private val expansionExponent: Double = 1.25,
    private val expansionRate: Double = 4.5
) {
    fun calculateSearchWindow(elapsedSeconds: Float): Int {
        val expansion = expansionRate * elapsedSeconds.toDouble().pow(expansionExponent)
        return min(maxWindowMmr, (baseWindowMmr + expansion).toInt())
    }
}

class NetworkClockSynchronizer {
    private var clockOffsetMs: Long = 0L
    private var roundTripTimeMs: Long = 0L

    fun processPong(t1ClientSend: Long, t2ServerRecv: Long, t3ServerSend: Long, t4ClientRecv: Long) {
        roundTripTimeMs = (t4ClientRecv - t1ClientSend) - (t3ServerSend - t2ServerRecv)
        clockOffsetMs = ((t2ServerRecv - t1ClientSend) + (t3ServerSend - t4ClientRecv)) / 2L
    }

    fun toLocalEpoch(serverEpochMs: Long): Long = serverEpochMs - clockOffsetMs
    fun getEstimatedServerTime(): Long = System.currentTimeMillis() + clockOffsetMs
    val currentRtt: Long get() = roundTripTimeMs
}

class EpochClockSynchronizer(
    private val totalMatchDurationSec: Int = 90
) {
    fun computeSecondsRemaining(startEpochMs: Long, currentLocalEpochMs: Long = System.currentTimeMillis()): Int {
        if (currentLocalEpochMs < startEpochMs) {
            return totalMatchDurationSec
        }
        val elapsedSec = ((currentLocalEpochMs - startEpochMs) / 1000L).toInt()
        return maxOf(0, totalMatchDurationSec - elapsedSec)
    }

    fun computeCountdownStep(startEpochMs: Long, currentLocalEpochMs: Long = System.currentTimeMillis()): Int {
        val deltaMs = startEpochMs - currentLocalEpochMs
        return if (deltaMs <= 0) 0 else minOf(3, ((deltaMs + 999) / 1000).toInt())
    }
}

class DeterministicPieceStream(seed: Long) {
    private val generator = SeededTrayGenerator(seed)

    fun nextTrayTrioShapes(): Array<PolyShape?> {
        return generator.nextTrayTrio()
    }
}
