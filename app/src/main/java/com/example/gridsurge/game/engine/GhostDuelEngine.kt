package com.example.gridsurge.game.engine

import android.util.Log
import com.example.gridsurge.game.replay.MatchReplayData
import kotlinx.coroutines.*

class GhostDuelEngine(
    private val scope: CoroutineScope,
    private val onRivalScoreUpdated: (Long) -> Unit,
    private val onLeadChanged: (isPlayerLeading: Boolean) -> Unit,
    private val onDuelFinished: (isPlayerWinner: Boolean, finalPlayerScore: Long, finalRivalScore: Long) -> Unit,
    private val onTimerTick: (secondsRemaining: Int) -> Unit,
    private val onSendEmpToPlayer: (Int) -> Unit,
    private val onRivalMoveLog: ((score: Long, combo: Int) -> Unit)? = null
) {
    var playerScoreProvider: (() -> Long)? = null

    var rivalScore: Long = 0L
        private set
    var rivalCombo: Int = 0
        private set
    var matchSecondsRemaining: Int = 90
        private set
    var isDuelActive: Boolean = false
        private set
    var isLiveMultiplayer: Boolean = false
        private set

    private var duelJob: Job? = null
    private var lastPlayerLeadState: Boolean? = null
    var matchSeed: Long = 0L
        private set

    var activeRivalReplay: MatchReplayData? = null
        private set

    fun updateRivalReplay(rivalReplay: MatchReplayData) {
        if (activeRivalReplay?.matchId == rivalReplay.matchId) return
        activeRivalReplay = rivalReplay
        Log.d("GhostDuelEngine", "Updated active rival replay: ${rivalReplay.matchId}")
    }

    private var matchStartEpochMs: Long = 0L

    /**
     * Synchronizes live duel timer against server-anchored start epoch.
     */
    fun updateLiveTimer(startEpochMs: Long, totalSeconds: Int = 90) {
        if (!isDuelActive) return
        val finishEpochMs = startEpochMs + (totalSeconds * 1000L)
        val now = System.currentTimeMillis()
        val remaining = ((finishEpochMs - now + 999) / 1000).toInt().coerceIn(0, totalSeconds)

        if (remaining != matchSecondsRemaining) {
            matchSecondsRemaining = remaining
            onTimerTick(matchSecondsRemaining)
            if (matchSecondsRemaining <= 0) {
                concludeMatchByTimeExpiry()
            }
        }
    }

    private fun concludeMatchByTimeExpiry() {
        isDuelActive = false
        duelJob?.cancel()
        val pScore = playerScoreProvider?.invoke() ?: 0L
        scope.launch(Dispatchers.Main) {
            onDuelFinished(pScore >= rivalScore, pScore, rivalScore)
        }
    }

    /**
     * Starts match in LIVE multiplayer mode (telemetry driven by WebSocket/Realtime anchored to server start epoch).
     */
    fun startLiveDuel(seed: Long, startEpochMs: Long, totalSeconds: Int = 90) {
        duelJob?.cancel()
        isDuelActive = true
        isLiveMultiplayer = true
        matchSeed = seed
        matchStartEpochMs = startEpochMs
        matchSecondsRemaining = totalSeconds
        onTimerTick(totalSeconds)
        updateLiveTimer(startEpochMs, totalSeconds)
        startEpochClockLoop(startEpochMs, totalSeconds)
    }

    /**
     * Starts match in LIVE multiplayer mode without explicit start epoch.
     */
    fun startLiveDuel(seed: Long, totalSeconds: Int = 90) {
        startLiveDuel(seed, System.currentTimeMillis(), totalSeconds)
    }

    /**
     * Starts match in GHOST replay or simulated bot mode.
     */
    fun startDuel(seed: Long, rivalReplay: MatchReplayData? = null, totalSeconds: Int = 90) {
        reset(totalSeconds)
        isDuelActive = true
        isLiveMultiplayer = false
        matchSeed = seed
        activeRivalReplay = rivalReplay
        matchSecondsRemaining = totalSeconds
        startGhostLoop()
    }

    /**
     * Receives live opponent action from Supabase Realtime broadcast.
     */
    fun onLiveRivalTelemetryReceived(
        remoteScore: Long,
        remoteLines: Int,
        remoteCombo: Int,
        isFever: Boolean,
        isTko: Boolean
    ) {
        if (!isDuelActive) return
        rivalScore = remoteScore
        rivalCombo = remoteCombo
        onRivalScoreUpdated(rivalScore)
        onRivalMoveLog?.invoke(rivalScore, rivalCombo)

        val myScore = playerScoreProvider?.invoke() ?: 0L
        evaluateLead(myScore)

        if (isTko) {
            concludeMatch(myScore)
        }
    }

    fun reset(initialSeconds: Int = 90) {
        duelJob?.cancel()
        rivalScore = 0L
        rivalCombo = 0
        matchSecondsRemaining = initialSeconds
        isDuelActive = false
        isLiveMultiplayer = false
        lastPlayerLeadState = null
        activeRivalReplay = null
        onRivalScoreUpdated(0L)
        onTimerTick(initialSeconds)
    }

    private fun startEpochClockLoop(startEpochMs: Long, totalSeconds: Int) {
        duelJob?.cancel()
        duelJob = scope.launch(Dispatchers.Main) {
            while (isActive && isDuelActive) {
                updateLiveTimer(startEpochMs, totalSeconds)
                delay(100L) // High-frequency sampling ensures zero clock drift
            }
        }
    }

    private fun startGhostLoop() {
        duelJob?.cancel()
        duelJob = scope.launch(Dispatchers.Main) {
            try {
                var rivalMoveTimer = 0L
                var nextRivalMoveDelay = (2000L..2500L).random()
                val replayMoves = activeRivalReplay?.playerMoves ?: emptyList()
                var replayIndex = 0

                while (isActive && isDuelActive) {
                    delay(100L)
                    rivalMoveTimer += 100L

                    // Clock Countdown
                    if (rivalMoveTimer % 1000L == 0L) {
                        matchSecondsRemaining = (matchSecondsRemaining - 1).coerceAtLeast(0)
                        onTimerTick(matchSecondsRemaining)
                        if (matchSecondsRemaining <= 0) {
                            isDuelActive = false
                            val pScore = playerScoreProvider?.invoke() ?: 0L
                            onDuelFinished(pScore >= rivalScore, pScore, rivalScore)
                            break
                        }
                    }

                    // Playback Recorded Replay or Simulated Bot
                    if (replayMoves.isNotEmpty()) {
                        while (replayIndex < replayMoves.size && replayMoves[replayIndex].timestampMs <= rivalMoveTimer) {
                            val move = replayMoves[replayIndex]
                            rivalScore = move.scoreAfterMove
                            rivalCombo = move.comboStreak
                            onRivalScoreUpdated(rivalScore)
                            onRivalMoveLog?.invoke(rivalScore, rivalCombo)
                            replayIndex++
                        }
                    } else if (rivalMoveTimer >= nextRivalMoveDelay) {
                        rivalMoveTimer = 0L
                        nextRivalMoveDelay = (2000L..2500L).random()

                        val isCombo = (1..100).random() <= 30
                        val points = if (isCombo) {
                            rivalCombo = (2..4).random()
                            (350L..650L).random() * rivalCombo
                        } else {
                            rivalCombo = 1
                            (120L..280L).random()
                        }

                        rivalScore += points
                        onRivalScoreUpdated(rivalScore)
                        onRivalMoveLog?.invoke(rivalScore, rivalCombo)
                    }
                }
            } catch (e: Exception) {
                Log.e("GhostDuelEngine", "Error in ghost loop", e)
            }
        }
    }

    fun evaluateLead(playerScore: Long) {
        if (!isDuelActive) return
        val isPlayerCurrentlyLeading = playerScore >= rivalScore

        if (lastPlayerLeadState != null && lastPlayerLeadState != isPlayerCurrentlyLeading) {
            onLeadChanged(isPlayerCurrentlyLeading)
        }
        lastPlayerLeadState = isPlayerCurrentlyLeading
    }

    fun concludeMatch(playerScore: Long) {
        isDuelActive = false
        duelJob?.cancel()
        val isWinner = playerScore > rivalScore
        scope.launch(Dispatchers.Main) {
            onDuelFinished(isWinner, playerScore, rivalScore)
        }
    }

    fun concludeMatchWithDefeat(playerScore: Long) {
        isDuelActive = false
        duelJob?.cancel()
        scope.launch(Dispatchers.Main) {
            onDuelFinished(false, playerScore, rivalScore)
        }
    }

    fun concludeMatchWithVictory(playerScore: Long) {
        isDuelActive = false
        duelJob?.cancel()
        scope.launch(Dispatchers.Main) {
            onDuelFinished(true, playerScore, rivalScore)
        }
    }
}
