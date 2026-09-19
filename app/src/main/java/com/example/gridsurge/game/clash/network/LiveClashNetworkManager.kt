package com.example.gridsurge.game.clash.network

import com.example.gridsurge.game.clash.model.RivalCombatant
import com.example.gridsurge.game.clash.network.engine.MatchmakingRangeCalculator
import com.example.gridsurge.game.clash.network.engine.NetworkClockSynchronizer
import com.example.gridsurge.game.clash.network.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LiveClashNetworkManager(
    private val serverUrl: String = "wss://clash.gridsurge.io/v1/arena",
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(5, TimeUnit.SECONDS)
        .build()

    private var activeWebSocket: WebSocket? = null
    private val clockSync = NetworkClockSynchronizer()
    private val rangeCalculator = MatchmakingRangeCalculator()

    private val _phase = MutableStateFlow(LiveMatchmakingPhase.IDLE)
    val phase: StateFlow<LiveMatchmakingPhase> = _phase.asStateFlow()

    private val _searchWindowMmr = MutableStateFlow(60)
    val searchWindowMmr: StateFlow<Int> = _searchWindowMmr.asStateFlow()

    private val _elapsedQueueTimeSec = MutableStateFlow(0)
    val elapsedQueueTimeSec: StateFlow<Int> = _elapsedQueueTimeSec.asStateFlow()

    private var currentSessionConfig: LiveMatchSessionConfig? = null
    var onSessionEstablished: ((LiveMatchSessionConfig) -> Unit)? = null
    var onRemoteCombatTelemetryReceived: ((ClashNetworkPacket.CombatTelemetrySync) -> Unit)? = null
    var onOpponentDisconnected: (() -> Unit)? = null

    private var queueTickerJob: Job? = null
    private var sequenceCounter = 0L

    /**
     * Enters the live competitive matchmaking pool.
     */
    fun enterMatchmakingQueue(ticket: MatchmakingTicket) {
        if (_phase.value != LiveMatchmakingPhase.IDLE) return
        _phase.value = LiveMatchmakingPhase.CONNECTING_TO_LOBBY
        _elapsedQueueTimeSec.value = 0

        val request = Request.Builder().url(serverUrl).build()
        activeWebSocket = client.newWebSocket(request, createWebSocketListener(ticket))

        // Start Local Queue Ticker
        queueTickerJob?.cancel()
        queueTickerJob = scope.launch {
            while (isActive && _phase.value == LiveMatchmakingPhase.SEARCHING_QUEUE) {
                delay(1000L)
                val newSec = _elapsedQueueTimeSec.value + 1
                _elapsedQueueTimeSec.value = newSec
                _searchWindowMmr.value = rangeCalculator.calculateSearchWindow(newSec.toFloat())
            }
        }
    }

    /**
     * Sends local drop & line-clear actions to the opponent.
     */
    fun dispatchCombatTelemetry(
        currentScore: Long,
        linesCleared: Int,
        comboStreak: Int,
        isFeverActive: Boolean,
        isToppedOut: Boolean = false
    ) {
        val session = currentSessionConfig ?: return
        val packet = ClashNetworkPacket.CombatTelemetrySync(
            roomId = session.roomId,
            sequenceId = ++sequenceCounter,
            currentScore = currentScore,
            linesClearedThisMove = linesCleared,
            comboStreak = comboStreak,
            isFeverActive = isFeverActive,
            isMatrixToppedOut = isToppedOut
        )

        val json = JSONObject().apply {
            put("action", "COMBAT_SYNC")
            put("room_id", packet.roomId)
            put("seq", packet.sequenceId)
            put("score", packet.currentScore)
            put("lines", packet.linesClearedThisMove)
            put("combo", packet.comboStreak)
            put("fever", packet.isFeverActive)
            put("tko", packet.isMatrixToppedOut)
        }

        activeWebSocket?.send(json.toString())
    }

    fun cancelQueue() {
        queueTickerJob?.cancel()
        activeWebSocket?.close(1000, "PLAYER_CANCELLED")
        activeWebSocket = null
        _phase.value = LiveMatchmakingPhase.IDLE
    }

    private fun createWebSocketListener(ticket: MatchmakingTicket) = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _phase.value = LiveMatchmakingPhase.SEARCHING_QUEUE

            // Send Join Ticket
            val joinPayload = JSONObject().apply {
                put("action", "ENTER_QUEUE")
                put("player_id", ticket.playerId)
                put("callsign", ticket.callsign)
                put("mmr", ticket.ratingMmr)
                put("version", ticket.clientVersion)
                put("region", ticket.region)
            }
            webSocket.send(joinPayload.toString())
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val json = JSONObject(text)
            when (json.optString("type")) {
                "MATCH_FOUND" -> {
                    queueTickerJob?.cancel()
                    _phase.value = LiveMatchmakingPhase.MATCH_RESERVED

                    val serverStartMs = json.getLong("start_epoch_ms")
                    val seed = json.getLong("shared_seed")
                    val roomId = json.getString("room_id")

                    val rival = RivalCombatant(
                        callsign = json.getString("rival_callsign"),
                        tierTitle = json.getString("rival_tier"),
                        currentMmr = json.getInt("rival_mmr"),
                        targetScore = 0L,
                        avgPpm = 35
                    )

                    val config = LiveMatchSessionConfig(
                        roomId = roomId,
                        sharedMatchSeed = seed,
                        matchStartEpochMs = clockSync.toLocalEpoch(serverStartMs),
                        rivalProfile = rival
                    )
                    currentSessionConfig = config

                    _phase.value = LiveMatchmakingPhase.STAGING_COUNTDOWN
                    scope.launch(Dispatchers.Main) {
                        onSessionEstablished?.invoke(config)
                    }
                }

                "COMBAT_RELAY" -> {
                    val syncPacket = ClashNetworkPacket.CombatTelemetrySync(
                        roomId = json.getString("room_id"),
                        sequenceId = json.getLong("seq"),
                        currentScore = json.getLong("score"),
                        linesClearedThisMove = json.getInt("lines"),
                        comboStreak = json.getInt("combo"),
                        isFeverActive = json.getBoolean("fever"),
                        isMatrixToppedOut = json.getBoolean("tko")
                    )
                    scope.launch(Dispatchers.Main) {
                        onRemoteCombatTelemetryReceived?.invoke(syncPacket)
                    }
                }

                "PONG" -> {
                    val t1 = json.getLong("client_t1")
                    val t2 = json.getLong("server_t2")
                    val t3 = json.getLong("server_t3")
                    val t4 = System.currentTimeMillis()
                    clockSync.processPong(t1, t2, t3, t4)
                }

                "OPPONENT_DISCONNECTED" -> {
                    _phase.value = LiveMatchmakingPhase.OPPONENT_DISCONNECTED
                    scope.launch(Dispatchers.Main) {
                        onOpponentDisconnected?.invoke()
                    }
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            queueTickerJob?.cancel()
            _phase.value = LiveMatchmakingPhase.ERROR_TIMEOUT
        }
    }
}
