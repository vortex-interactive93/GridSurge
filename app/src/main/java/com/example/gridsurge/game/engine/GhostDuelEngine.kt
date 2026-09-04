package com.example.gridsurge.game.engine

import android.util.Log
import com.example.gridsurge.game.replay.MatchReplayData
import kotlinx.coroutines.*
import java.util.Random

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
    var matchSecondsRemaining: Int = 75
        private set
    var isDuelActive: Boolean = false
        private set

    private var duelJob: Job? = null
    private var lastPlayerLeadState: Boolean? = null
    var matchSeed: Long = 0L
        private set

    var activeRivalReplay: MatchReplayData? = null
        private set

    fun startDuel(seed: Long, rivalReplay: MatchReplayData? = null) {
        reset()
        isDuelActive = true
        matchSeed = seed
        activeRivalReplay = rivalReplay
        startDuelLoop()
    }

    fun reset() {
        duelJob?.cancel()
        rivalScore = 0L
        rivalCombo = 0
        matchSecondsRemaining = 75
        isDuelActive = false
        lastPlayerLeadState = null
        activeRivalReplay = null
        onRivalScoreUpdated(0L)
        onTimerTick(75)
    }

    private fun startDuelLoop() {
        duelJob?.cancel()
        duelJob = scope.launch {
            try {
                var rivalMoveTimer = 0L
                var nextRivalMoveDelay = (2000L..2500L).random()
                val replayMoves = activeRivalReplay?.playerMoves ?: emptyList()
                var replayIndex = 0

                while (isActive && isDuelActive) {
                    delay(100)
                    rivalMoveTimer += 100

                    // 1. Clock Countdown
                    if (rivalMoveTimer % 1000 == 0L) {
                        matchSecondsRemaining = (matchSecondsRemaining - 1).coerceAtLeast(0)
                        onTimerTick(matchSecondsRemaining)
                        if (matchSecondsRemaining <= 0) {
                            isDuelActive = false
                            val pScore = playerScoreProvider?.invoke() ?: 0L
                            scope.launch(Dispatchers.Main) {
                                onDuelFinished(pScore >= rivalScore, pScore, rivalScore)
                            }
                            break // Time's up!
                        }
                    }

                    // 2. Playback Real Player Replay IF available, ELSE run AI Bot Loop
                    if (replayMoves.isNotEmpty()) {
                        while (replayIndex < replayMoves.size && replayMoves[replayIndex].timestampMs <= rivalMoveTimer) {
                            val move = replayMoves[replayIndex]
                            rivalScore = move.scoreAfterMove
                            rivalCombo = move.comboStreak
                            onRivalScoreUpdated(rivalScore)
                            onRivalMoveLog?.invoke(rivalScore, rivalCombo)

                            if (move.comboStreak > 1 && rivalScore > 1000 && (1..100).random() <= 10) {
                                onSendEmpToPlayer((0..2).random())
                            }
                            replayIndex++
                        }
                    } else if (rivalMoveTimer >= nextRivalMoveDelay) {
                        rivalMoveTimer = 0L
                        nextRivalMoveDelay = (2000L..2500L).random()

                        val moveRoll = (1..100).random()
                        val isCombo = moveRoll <= 30
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

                        // 10% chance on combo move to disrupt player with EMP
                        if (isCombo && rivalScore > 1000 && (1..100).random() <= 10) {
                            onSendEmpToPlayer((0..2).random())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GhostDuelEngine", "Error in duel loop", e)
            }
        }
    }

    fun evaluateLead(playerScore: Long) {
        if (!isDuelActive) return
        val isPlayerCurrentlyLeading = playerScore >= rivalScore

        // Trigger lead change audio ONLY when lead flips
        if (lastPlayerLeadState != null && lastPlayerLeadState != isPlayerCurrentlyLeading) {
            onLeadChanged(isPlayerCurrentlyLeading)
        }
        lastPlayerLeadState = isPlayerCurrentlyLeading
    }

    fun onPlayerAttacked(attackType: String) {
        // Future: Could disrupt bot
    }

    fun concludeMatch(playerScore: Long) {
        isDuelActive = false
        duelJob?.cancel()
        val isWinner = playerScore >= rivalScore
        onDuelFinished(isWinner, playerScore, rivalScore)
    }

    fun onPlayerAction(linesCleared: Int, comboStreak: Int) {
        // Future: Could impact ghost AI
    }
}
