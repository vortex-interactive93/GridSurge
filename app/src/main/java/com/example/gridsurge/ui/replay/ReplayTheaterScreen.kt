package com.example.gridsurge.ui.replay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.game.replay.MatchReplayData
import com.example.gridsurge.game.replay.ReplayMove
import kotlinx.coroutines.delay

@Composable
fun ReplayTheaterScreen(
    replayData: MatchReplayData,
    onClose: () -> Unit
) {
    val totalDurationMs = (replayData.matchDurationSec * 1000L).coerceAtLeast(1000L)
    var currentPlaybackTimeMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Playback loop
    LaunchedEffect(isPlaying, playbackSpeed) {
        val frameIntervalMs = 33L // ~30 FPS timeline refresh
        while (isPlaying) {
            delay(frameIntervalMs)
            currentPlaybackTimeMs = (currentPlaybackTimeMs + (frameIntervalMs * playbackSpeed).toLong())
            if (currentPlaybackTimeMs >= totalDurationMs) {
                currentPlaybackTimeMs = totalDurationMs
                isPlaying = false
            }
        }
    }

    // Reconstruct board states and telemetry at the current timestamp
    val playerActiveMoves = remember(currentPlaybackTimeMs) {
        replayData.playerMoves.filter { it.timestampMs <= currentPlaybackTimeMs }
    }
    val rivalActiveMoves = remember(currentPlaybackTimeMs) {
        replayData.rivalMoves.filter { it.timestampMs <= currentPlaybackTimeMs }
    }

    val currentPlayerScore = playerActiveMoves.lastOrNull()?.scoreAfterMove ?: 0L
    val currentRivalScore = rivalActiveMoves.lastOrNull()?.scoreAfterMove ?: 0L
    val lastMove = playerActiveMoves.lastOrNull()

    // 8x8 Board Reconstruction
    val gridState = remember(playerActiveMoves) {
        reconstructGrid(playerActiveMoves)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03070E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- 1. HEADER & META ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEURAL REPLAY THEATER",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "MATCH ID // ${replayData.matchId}",
                        color = Color(0xFF8090A0),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Replay",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }

            // --- 2. LIVE DUEL TELEMETRY BAR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "YOU: $currentPlayerScore",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "RIVAL: $currentRivalScore",
                        color = Color(0xFFFF0055),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Momentum Delta Rail
                val delta = (currentPlayerScore - currentRivalScore).coerceIn(-3000L, 3000L)
                val railNormalized = (delta + 3000f) / 6000f

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    drawRoundRect(
                        color = Color(0xFF101B2B),
                        size = size,
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    val pinX = size.width * railNormalized
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF007799))),
                        topLeft = Offset(0f, 0f),
                        size = Size(pinX, size.height)
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF990033), Color(0xFFFF0055))),
                        topLeft = Offset(pinX, 0f),
                        size = Size(size.width - pinX, size.height)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(pinX, size.height / 2f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val trueDelta = currentPlayerScore - currentRivalScore
                Text(
                    text = if (trueDelta >= 0) "[ +$trueDelta LEAD ]" else "[ $trueDelta DEFICIT ]",
                    color = if (trueDelta >= 0) Color(0xFF00E5FF) else Color(0xFFFF0055),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- 3. REPLAY BOARD CANVAS (8x8 Grid) ---
            ReplayBoardGrid(gridState = gridState)

            // Last Event Action Pill
            Surface(
                color = Color(0xFF0F1B2C),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFEA80FC)))),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = lastMove?.let {
                        "MOVE #${playerActiveMoves.size} // ${if (it.linesCleared > 0) "${it.linesCleared} LINE CLEAR! (x${it.comboStreak})" else "DROP"}"
                    } ?: "MATCH INITIALIZED",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // --- 4. PLAYBACK CONTROLS & TIMELINE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070F1E), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time Readout
                val currentSec = currentPlaybackTimeMs / 1000
                val totalSec = totalDurationMs / 1000
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", currentSec / 60, currentSec % 60),
                        color = Color(0xFF00E5FF),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%02d:%02d", totalSec / 60, totalSec % 60),
                        color = Color(0xFF8090A0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                // Interactive Scrub Slider
                Slider(
                    value = currentPlaybackTimeMs.toFloat(),
                    onValueChange = {
                        isPlaying = false
                        currentPlaybackTimeMs = it.toLong()
                    },
                    valueRange = 0f..totalDurationMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF16253B)
                    )
                )

                // Control Buttons: Jump to Start, Play/Pause, Speed Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset / Jump to Start
                    TextButton(
                        onClick = {
                            currentPlaybackTimeMs = 0L
                            isPlaying = true
                        }
                    ) {
                        Text("RESTART", color = Color(0xFF8090A0), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Play / Pause FAB
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF00E5FF), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black
                        )
                    }

                    // 1x / 2x / 4x Speed Multiplier Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF101B2B),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.clickable {
                            playbackSpeed = when (playbackSpeed) {
                                1.0f -> 2.0f
                                2.0f -> 4.0f
                                else -> 1.0f
                            }
                        }
                    ) {
                        Text(
                            text = "${playbackSpeed.toInt()}x SPEED",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reconstructs the exact 8x8 grid color layout by placing full polyominoes
 * with their captured colors and sweeping cleared lines sequentially.
 */
fun reconstructGrid(moves: List<ReplayMove>): IntArray {
    val grid = IntArray(64) { 0 }

    for (move in moves) {
        // 1. Place every cell belonging to the polyomino with its original theme color
        for ((dr, dc) in move.occupiedOffsets) {
            val r = move.targetRow + dr
            val c = move.targetCol + dc
            if (r in 0 until 8 && c in 0 until 8) {
                grid[r * 8 + c] = move.colorInt
            }
        }

        // 2. Identify all completed rows & columns simultaneously
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until 8) {
            if ((0 until 8).all { c -> grid[r * 8 + c] != 0 }) {
                rowsToClear.add(r)
            }
        }
        for (c in 0 until 8) {
            if ((0 until 8).all { r -> grid[r * 8 + c] != 0 }) {
                colsToClear.add(c)
            }
        }

        // 3. Clear intersecting full lines
        for (r in rowsToClear) {
            for (c in 0 until 8) {
                grid[r * 8 + c] = 0
            }
        }
        for (c in colsToClear) {
            for (r in 0 until 8) {
                grid[r * 8 + c] = 0
            }
        }
    }

    return grid
}

@Composable
fun ReplayBoardGrid(
    gridState: IntArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(320.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(Color(0xFF060D1A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / 8f
            val padding = 2.dp.toPx()
            val tileSize = cellSize - (padding * 2)

            // 1. Draw Empty Slot Backgrounds
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    drawRoundRect(
                        color = Color(0xFF0A1526),
                        topLeft = Offset(c * cellSize + padding, r * cellSize + padding),
                        size = Size(tileSize, tileSize),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // 2. Draw Multi-Colored Blocks with Beveled Outlines
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val colorInt = gridState[r * 8 + c]
                    if (colorInt != 0) {
                        val baseColor = Color(colorInt)
                        val tileTopLeft = Offset(c * cellSize + padding, r * cellSize + padding)

                        // Main Block Fill
                        drawRoundRect(
                            color = baseColor,
                            topLeft = tileTopLeft,
                            size = Size(tileSize, tileSize),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Inner Beveled Highlight (Gives Depth to Polyomino Shapes)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.35f),
                            topLeft = tileTopLeft,
                            size = Size(tileSize, tileSize),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Outer Glow Ring
                        drawRoundRect(
                            color = baseColor.copy(alpha = 0.5f),
                            topLeft = Offset(c * cellSize + 0.5f, r * cellSize + 0.5f),
                            size = Size(cellSize - 1f, cellSize - 1f),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
