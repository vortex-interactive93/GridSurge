package com.example.gridsurge.game.clash.network

import android.util.Log
import com.example.gridsurge.game.clash.network.model.LiveCombatPacket
import com.example.gridsurge.game.clash.network.model.MatchFindResponse
import com.example.gridsurge.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.json.JSONObject

class SupabaseClashRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val tag = "CLASH_TELEMETRY"
    private var combatChannel: RealtimeChannel? = null
    private var packetCollectorJob: Job? = null
    private var statusCollectorJob: Job? = null

    @Volatile private var isChannelReady = false
    @Volatile private var currentLocalUserId = ""
    @Volatile private var pendingMovePacket: JsonObject? = null

    var onRemoteCombatReceived: ((LiveCombatPacket) -> Unit)? = null
    var onRemoteEmoteReceived: ((emoteId: String) -> Unit)? = null
    var onRemoteHandshakeReceived: ((avatarKey: String, equippedBadgesStr: String) -> Unit)? = null
    var onOpponentDropped: (() -> Unit)? = null

    /**
     * Polls Supabase matchmaking procedure and pre-warms the WebSocket transport.
     */
    suspend fun findMatch(
        userId: String,
        callsign: String,
        currentMmr: Int,
        avatarKey: String = "avatar_caucasian_male",
        equippedBadges: String = "DECA_SURGE,GRID_NULLIFIER,FOUNDER",
        onSearching: (searchWindow: Int, elapsedSec: Int) -> Unit
    ): MatchFindResponse = withContext(Dispatchers.IO) {
        var elapsedSec = 0
        var searchWindow = 60

        val cleanUserId = userId.ifBlank { "OPERATOR_${System.currentTimeMillis() % 10000}" }
        val cleanCallsign = callsign.ifBlank { "OPERATOR" }

        // Pre-warm WebSocket transport during queue search
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(tag, ">>> Pre-warming Realtime WebSocket transport...")
                SupabaseClientProvider.client.realtime.connect()
            } catch (e: Throwable) {
                Log.w(tag, "Realtime pre-warm warning: ${e.message}")
            }
        }

        Log.d(tag, ">>> Match search: user=$cleanUserId, callsign=$cleanCallsign, mmr=$currentMmr, avatar=$avatarKey")

        while (isActive) {
            val rawData = try {
                SupabaseClientProvider.client.postgrest.rpc(
                    function = "find_or_create_clash_match",
                    parameters = buildJsonObject {
                        put("p_player_id", cleanUserId)
                        put("p_callsign", cleanCallsign)
                        put("p_mmr", currentMmr)
                        put("p_search_window", searchWindow)
                        put("p_avatar_key", avatarKey)
                        put("p_equipped_badges", equippedBadges)
                    }
                ).data
            } catch (e: Throwable) {
                Log.w(tag, "Extended RPC failed, trying legacy params: ${e.message}")
                SupabaseClientProvider.client.postgrest.rpc(
                    function = "find_or_create_clash_match",
                    parameters = buildJsonObject {
                        put("p_player_id", cleanUserId)
                        put("p_callsign", cleanCallsign)
                        put("p_mmr", currentMmr)
                        put("p_search_window", searchWindow)
                    }
                ).data
            }

            val json = JSONObject(rawData)
            val status = json.optString("status")

            if (status == "MATCH_FOUND") {
                val roomId = json.optString("room_id")
                val sharedSeed = json.optLong("shared_seed")
                val startEpochMs = json.optLong("start_epoch_ms")
                val rivalId = json.optString("rival_id")
                val rivalCallsign = json.optString("rival_callsign")
                val rivalMmr = json.optInt("rival_mmr", currentMmr)
                var rivalAvatarKey = json.optString("rival_avatar_key")
                var rivalEquippedBadges = json.optString("rival_equipped_badges")

                if (rivalId.isNotBlank() && (rivalAvatarKey.isBlank() || rivalEquippedBadges.isBlank())) {
                    val fetched = SupabaseProfileRepository.fetchOpponentProfile(rivalId).getOrNull()
                    if (fetched != null) {
                        if (rivalAvatarKey.isBlank() && fetched.avatarId.isNotBlank()) {
                            rivalAvatarKey = fetched.avatarId
                        }
                        if (rivalEquippedBadges.isBlank() && fetched.equippedBadges.isNotEmpty()) {
                            rivalEquippedBadges = fetched.equippedBadges.joinToString(",")
                        }
                    }
                }

                Log.d(tag, ">>> MATCH FOUND: room=$roomId, rival=$rivalCallsign, avatar=$rivalAvatarKey")
                return@withContext MatchFindResponse(
                    status = "MATCH_FOUND",
                    room_id = roomId,
                    shared_seed = sharedSeed,
                    start_epoch_ms = startEpochMs,
                    rival_id = rivalId,
                    rival_callsign = rivalCallsign,
                    rival_mmr = rivalMmr,
                    rival_avatar_key = rivalAvatarKey.ifBlank { null },
                    rival_equipped_badges = rivalEquippedBadges.ifBlank { null }
                )
            }

            withContext(Dispatchers.Main) { onSearching(searchWindow, elapsedSec) }
            delay(1200L)
            elapsedSec += 1
            searchWindow = (60 + (elapsedSec * 6.0).toInt()).coerceAtMost(400)
        }

        throw CancellationException("Matchmaking search aborted.")
    }

    /**
     * Connects to the room channel using non-blocking subscription and reactive status observing.
     */
    suspend fun joinMatchRoom(roomId: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            currentLocalUserId = userId
            isChannelReady = false
            pendingMovePacket = null

            val realtime = SupabaseClientProvider.client.realtime

            // Clean up previous channel if exists
            combatChannel?.let {
                try { realtime.removeChannel(it) } catch (_: Throwable) {}
            }

            // Connect socket asynchronously (non-blocking)
            realtime.connect()

            val channelName = "clash_room:$roomId"
            Log.d(tag, ">>> Configuring channel: $channelName with broadcast enabled")

            val channel = realtime.channel(channelName) {
                broadcast {
                    receiveOwnBroadcasts = false
                }
            }
            combatChannel = channel

            // Status Observer: Activates broadcast capability as soon as Phoenix ACKs the join
            statusCollectorJob?.cancel()
            statusCollectorJob = scope.launch(Dispatchers.IO) {
                channel.status.collect { status ->
                    Log.d(tag, ">>> Realtime Channel Status -> $status on $channelName")
                    if (status == RealtimeChannel.Status.SUBSCRIBED) {
                        isChannelReady = true
                        Log.d(tag, ">>> Realtime Broadcast channel ACTIVE!")

                        pendingMovePacket?.let { buffered ->
                            pendingMovePacket = null
                            Log.d(tag, ">>> [FLUSHING BUFFERED MOVE]: $buffered")
                            try {
                                channel.broadcast(event = "COMBAT_SYNC", message = buffered)
                            } catch (e: Throwable) {
                                Log.e(tag, "Failed flushing buffered packet: ${e.message}")
                            }
                        }
                    } else {
                        isChannelReady = false
                    }
                }
            }

            // Inbound Packet Collector (Raw JsonObject)
            packetCollectorJob?.cancel()
            packetCollectorJob = scope.launch(Dispatchers.IO) {
                try {
                    Log.d(tag, ">>> Listening for COMBAT_SYNC broadcasts on $channelName...")
                    channel.broadcastFlow<JsonObject>("COMBAT_SYNC").collect { rawJson ->
                        Log.d(tag, ">>> [RAW BYTES RECEIVED]: $rawJson")
                        val senderId = rawJson["sender_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        if (senderId == currentLocalUserId) {
                            return@collect
                        }

                        val score = rawJson["score"]?.jsonPrimitive?.longOrNull ?: 0L
                        val lines = rawJson["lines"]?.jsonPrimitive?.intOrNull ?: 0
                        val combo = rawJson["combo"]?.jsonPrimitive?.intOrNull ?: 0
                        val fever = rawJson["fever"]?.jsonPrimitive?.booleanOrNull ?: false
                        val tko = rawJson["tko"]?.jsonPrimitive?.booleanOrNull ?: false
                        val apm = rawJson["apm"]?.jsonPrimitive?.intOrNull ?: 0
                        val flushes = rawJson["flushes"]?.jsonPrimitive?.intOrNull ?: 0

                        Log.d(tag, ">>> [PACKET RECEIVED] Sender: $senderId, Score:$score, Lines: $lines, APM:$apm, Flushes:$flushes")
                        withContext(Dispatchers.Main) {
                            onRemoteCombatReceived?.invoke(
                                LiveCombatPacket(senderId, score, lines, combo, fever, tko, apm, flushes)
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    Log.d(tag, ">>> Packet collector closed.")
                } catch (e: Throwable) {
                    Log.e(tag, ">>> [CRITICAL] Packet collector crashed: ${e.message}")
                }
            }

            // Inbound Emote Listener
            scope.launch(Dispatchers.IO) {
                try {
                    channel.broadcastFlow<JsonObject>("EMOTE_SYNC").collect { rawJson ->
                        val senderId = rawJson["sender_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        if (senderId == currentLocalUserId) return@collect // Ignore echo

                        val emoteId = rawJson["emote_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        if (emoteId.isNotBlank()) {
                            withContext(Dispatchers.Main) {
                                onRemoteEmoteReceived?.invoke(emoteId)
                            }
                        }
                    }
                } catch (_: CancellationException) {
                } catch (e: Throwable) {
                    Log.e(tag, "Emote collector crashed: ${e.message}")
                }
            }

            // Inbound Handshake Listener
            scope.launch(Dispatchers.IO) {
                try {
                    channel.broadcastFlow<JsonObject>("HANDSHAKE_SYNC").collect { rawJson ->
                        val senderId = rawJson["sender_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        if (senderId == currentLocalUserId) return@collect // Ignore echo

                        val rivalAvatarKey = rawJson["avatar_key"]?.jsonPrimitive?.contentOrNull ?: ""
                        val rivalBadgesStr = rawJson["equipped_badges"]?.jsonPrimitive?.contentOrNull ?: ""

                        if (rivalAvatarKey.isNotBlank()) {
                            Log.d(tag, ">>> [HANDSHAKE RECEIVED] Sender:$senderId, Avatar:$rivalAvatarKey, Badges:$rivalBadgesStr")
                            withContext(Dispatchers.Main) {
                                onRemoteHandshakeReceived?.invoke(rivalAvatarKey, rivalBadgesStr)
                            }
                        }
                    }
                } catch (_: CancellationException) {
                } catch (e: Throwable) {
                    Log.e(tag, "Handshake collector crashed: ${e.message}")
                }
            }

            // Non-blocking subscribe: never throws 10,000ms timeout!
            Log.d(tag, ">>> Calling channel.subscribe(blockUntilSubscribed = false)...")
            channel.subscribe(blockUntilSubscribed = false)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(tag, ">>> Error joining Realtime channel: ${e.message}", e)
        }
    }

    /**
     * Broadcasts local player profile handshake over WebSockets.
     */
    fun broadcastHandshake(avatarKey: String, equippedBadgesStr: String) {
        val channel = combatChannel ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val payload = buildJsonObject {
                    put("sender_id", currentLocalUserId)
                    put("avatar_key", avatarKey)
                    put("equipped_badges", equippedBadgesStr)
                }
                channel.broadcast(event = "HANDSHAKE_SYNC", message = payload)
                Log.d(tag, ">>> [HANDSHAKE BROADCAST SENT]: avatar=$avatarKey, badges=$equippedBadgesStr")
            } catch (e: Throwable) {
                Log.w(tag, "Failed sending handshake broadcast: ${e.message}")
            }
        }
    }

    /**
     * Broadcasts local piece drop telemetry over WebSockets.
     */
    fun broadcastMove(
        score: Long,
        lines: Int,
        combo: Int,
        isFever: Boolean,
        isTko: Boolean = false,
        apm: Int = 0,
        flushes: Int = 0
    ) {
        val channel = combatChannel
        if (channel == null) {
            Log.w(tag, ">>> [BROADCAST ABORTED] combatChannel is null!")
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val payload = buildJsonObject {
                    put("sender_id", currentLocalUserId)
                    put("score", score)
                    put("lines", lines)
                    put("combo", combo)
                    put("fever", isFever)
                    put("tko", isTko)
                    put("apm", apm)
                    put("flushes", flushes)
                }

                if (!isChannelReady) {
                    Log.w(tag, ">>> [BROADCAST BUFFERED] Channel not yet SUBSCRIBED. Storing score: $score")
                    pendingMovePacket = payload
                    return@launch
                }

                Log.d(tag, ">>> [BROADCASTING MOVE] Sender: $currentLocalUserId, Score:$score, Lines: $lines, TKO:$isTko")
                channel.broadcast(
                    event = "COMBAT_SYNC",
                    message = payload
                )
            } catch (e: Throwable) {
                Log.e(tag, ">>> Broadcast failed: ${e.message}", e)
            }
        }
    }

    /**
     * Broadcasts live emote to the rival device.
     */
    fun broadcastEmote(emoteId: String) {
        val channel = combatChannel ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val payload = buildJsonObject {
                    put("sender_id", currentLocalUserId)
                    put("emote_id", emoteId)
                    put("ts", System.currentTimeMillis())
                }
                channel.broadcast(event = "EMOTE_SYNC", message = payload)
            } catch (e: Throwable) {
                Log.e(tag, "Failed broadcasting emote: ${e.message}")
            }
        }
    }

    suspend fun concludeMatch(roomId: String, winnerId: String, pScore: Long, rScore: Long) = withContext(Dispatchers.IO) {
        try {
            SupabaseClientProvider.client.postgrest.rpc(
                "conclude_clash_match",
                buildJsonObject {
                    put("p_room_id", roomId)
                    put("p_winner_id", winnerId)
                    put("p_player_a_score", pScore)
                    put("p_player_b_score", rScore)
                }
            )
            Log.d(tag, ">>> Concluded match $roomId on database.")
        } catch (e: Throwable) {
            Log.e(tag, ">>> Failed concluding match: ${e.message}")
        }
    }

    suspend fun cancelQueue(userId: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseClientProvider.client.postgrest.from("clash_queue").delete {
                filter { eq("user_id", userId) }
            }
        } catch (e: Throwable) {}
    }

    suspend fun disconnectRoom() = withContext(Dispatchers.IO) {
        try {
            isChannelReady = false
            pendingMovePacket = null
            statusCollectorJob?.cancel()
            packetCollectorJob?.cancel()

            combatChannel?.let { channel ->
                SupabaseClientProvider.client.realtime.removeChannel(channel)
            }
            combatChannel = null

            Log.d(tag, ">>> Disconnected from Realtime room.")
        } catch (e: Throwable) {
            Log.e(tag, ">>> Error disconnecting room: ${e.message}")
        }
    }
}
