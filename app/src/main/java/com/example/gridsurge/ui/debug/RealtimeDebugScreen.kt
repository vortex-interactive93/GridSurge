package com.example.gridsurge.ui.debug

import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.network.SupabaseClientProvider
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

@Composable
fun RealtimeDebugScreen(onClose: () -> Unit) {
    val tag = "PING_TEST"
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("DISCONNECTED") }
    val receivedLogs = remember { mutableStateListOf<String>() }
    var channelRef by remember { mutableStateOf<RealtimeChannel?>(null) }
    val deviceModel = remember { Build.MODEL }

    DisposableEffect(Unit) {
        val realtime = SupabaseClientProvider.client.realtime
        // Hardcoded channel with self-broadcast enabled for immediate loopback proof
        val channel = realtime.channel("debug_ping_pong_room") {
            broadcast {
                receiveOwnBroadcasts = true
            }
        }
        channelRef = channel

        val statusJob = scope.launch(Dispatchers.IO) {
            channel.status.collect { status ->
                Log.d(tag, "Channel status: $status")
                withContext(Dispatchers.Main) { statusText = status.name }
            }
        }

        val packetJob = scope.launch(Dispatchers.IO) {
            channel.broadcastFlow<JsonObject>("PING_EVENT").collect { json ->
                val sender = json["sender"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                val score = json["score"]?.jsonPrimitive?.longOrNull ?: 0L
                val logEntry = "[RECV] From $sender | Score: $score"
                Log.d(tag, logEntry)
                withContext(Dispatchers.Main) {
                    receivedLogs.add(0, logEntry)
                }
            }
        }

        scope.launch(Dispatchers.IO) {
            Log.d(tag, "Connecting realtime & subscribing...")
            realtime.connect()
            channel.subscribe(blockUntilSubscribed = false)
        }

        onDispose {
            statusJob.cancel()
            packetJob.cancel()
            scope.launch(Dispatchers.IO) {
                realtime.removeChannel(channel)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Text("REALTIME WIRE TEST", color = Color.Cyan, fontSize = 20.sp)
        Text("Device: $deviceModel", color = Color.White, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        Text(
            text = "STATUS: $statusText",
            color = if (statusText == "SUBSCRIBED") Color.Green else Color.Yellow,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val ch = channelRef
                if (ch != null) {
                    scope.launch(Dispatchers.IO) {
                        val payload = buildJsonObject {
                            put("sender", deviceModel)
                            put("score", (100..999).random().toLong())
                        }
                        Log.d(tag, "Sending PING: $payload")
                        ch.broadcast(event = "PING_EVENT", message = payload)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SEND PING PACKET")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CLOSE")
        }

        Spacer(Modifier.height(16.dp))
        Text("PACKET LOG:", color = Color.Gray, fontSize = 12.sp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            items(receivedLogs) { log ->
                Text(log, color = Color.Green, fontSize = 13.sp)
            }
        }
    }
}
