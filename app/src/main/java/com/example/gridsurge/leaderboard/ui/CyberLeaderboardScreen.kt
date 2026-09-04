package com.example.gridsurge.leaderboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.glitch.DailyGlitchTier
import com.example.gridsurge.leaderboard.model.CloudLeaderboardEntry
import com.example.gridsurge.leaderboard.model.GameModeType
import com.example.gridsurge.leaderboard.model.LeaderboardSyncState
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import java.util.Locale

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGold = Color(0xFFFFD600)
private val NeonRed = Color(0xFFFF0055)
private val DarkVoidBackdrop = Color(0xFF03070E)
private val CardBackground = Color(0x66080E1A)
private val CardBorder = Color(0xFF1B2A42)

@Composable
fun CyberLeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoidBackdrop)
    ) {
        // Background Matrix Grid Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 42.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(Color(0x0A00E5FF), Offset(x * step, 0f), Offset(x * step, size.height), 1f)
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(Color(0x0A00E5FF), Offset(0f, y * step), Offset(size.width, y * step), 1f)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(CardBackground, CyberChamferShape)
                        .border(1.dp, CardBorder, CyberChamferShape)
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onNavigateBack()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("< HUB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GLOBAL NETWORK",
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "LEADERBOARD",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }

                // Live Sync Status Pulse
                Box(
                    modifier = Modifier
                        .background(Color(0x2600FF66), CyberChamferShape)
                        .border(1.dp, Color(0x6600FF66), CyberChamferShape)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("LIVE", color = Color(0xFF00FF66), fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameModeType.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) Color(0x3300E5FF) else CardBackground, CyberChamferShape)
                            .border(1.dp, if (isSelected) NeonCyan else CardBorder, CyberChamferShape)
                            .clickable { viewModel.selectMode(mode) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) NeonCyan else Color(0xFF7E8B9B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main List & Content State
            when (val state = syncState) {
                is LeaderboardSyncState.Loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonCyan, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SYNCHRONIZING REPLAY UPLINK...", color = Color(0xFF8FA3BF), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                is LeaderboardSyncState.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UPLINK INTERRUPTED", color = NeonRed, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(state.message, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(14.dp))
                            CyberActionButton("RETRY UPLINK", NeonCyan, isPrimary = true) {
                                viewModel.loadLeaderboard(selectedMode)
                            }
                        }
                    }
                }

                is LeaderboardSyncState.Success -> {
                    Column(modifier = Modifier.weight(1f)) {
                        // Top 3 Podium (If available)
                        if (state.topEntries.size >= 3) {
                            PodiumSection(top3 = state.topEntries.take(3))
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Scrollable Leaderboard Rows (Ranks 4..100)
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(CardBackground, CyberChamferShape)
                                .border(1.dp, CardBorder, CyberChamferShape)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val listToDisplay = if (state.topEntries.size >= 3) state.topEntries.drop(3) else state.topEntries
                            items(listToDisplay) { entry ->
                                CloudLeaderboardRow(entry = entry)
                            }
                        }
                    }

                    // Sticky User Rank Pinned Footer
                    if (state.currentUserEntry != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        StickyUserFooter(entry = state.currentUserEntry)
                    }
                }

                LeaderboardSyncState.Idle -> {}
            }
        }
    }
}

@Composable
private fun PodiumSection(top3: List<CloudLeaderboardEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // #2 Silver
        PodiumCard(entry = top3[1], rank = 2, color = Color(0xFFCFD8DC), modifier = Modifier.weight(1f).height(100.dp))
        // #1 Gold (Taller)
        PodiumCard(entry = top3[0], rank = 1, color = NeonGold, modifier = Modifier.weight(1.1f).height(115.dp))
        // #3 Bronze
        PodiumCard(entry = top3[2], rank = 3, color = Color(0xFFFF8A65), modifier = Modifier.weight(1f).height(90.dp))
    }
}

@Composable
private fun PodiumCard(
    entry: CloudLeaderboardEntry,
    rank: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val rankCrestRes = when (rank) {
        1 -> R.drawable.ic_rank_crest_gold
        2 -> R.drawable.ic_rank_crest_silver
        3 -> R.drawable.ic_rank_crest_bronze
        else -> null
    }

    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), Color(0x66080E1A))),
                shape = CyberChamferShape
            )
            .border(1.5.dp, color.copy(alpha = 0.8f), CyberChamferShape)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (rankCrestRes != null) {
                Image(
                    painter = painterResource(id = rankCrestRes),
                    contentDescription = "Rank #$rank",
                    modifier = Modifier.size(42.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(text = "#$rank", color = color, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
            Text(text = entry.callsign, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text(text = String.format(Locale.US, "%,d", entry.score), color = color, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun CloudLeaderboardRow(entry: CloudLeaderboardEntry) {
    val rowBg = if (entry.isCurrentUser) Color(0x3300E5FF) else Color(0x1A0F172A)
    val rowBorder = if (entry.isCurrentUser) NeonCyan else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg, CyberChamferShape)
            .border(1.dp, rowBorder, CyberChamferShape)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = String.format(Locale.US, "#%02d", entry.rank), color = Color(0xFF7E8B9B), fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            
            if (entry.badgeResId != 0) {
                Image(
                    painter = painterResource(id = entry.badgeResId),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = entry.callsign, color = if (entry.isCurrentUser) NeonCyan else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    if (entry.verified) {
                        Text("✓", color = Color(0xFF00FF66), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (entry.title.isNotEmpty()) {
                    Text(
                        text = entry.title,
                        color = Color(0xFF5C8599),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(text = String.format(Locale.US, "%,d", entry.score), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun StickyUserFooter(entry: CloudLeaderboardEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0x3300E5FF), Color(0x1A080D1A))),
                shape = CyberChamferShape
            )
            .border(1.5.dp, NeonCyan, CyberChamferShape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (entry.badgeResId != 0) {
                    Image(
                        painter = painterResource(id = entry.badgeResId),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column {
                    Text("YOUR GLOBAL STANDING", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Text(entry.callsign, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    if (entry.title.isNotEmpty()) {
                        Text(
                            text = entry.title,
                            color = Color(0xFF5C8599),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("RANK #${entry.rank}", color = NeonGold, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text("${String.format(Locale.US, "%,d", entry.score)} PTS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
