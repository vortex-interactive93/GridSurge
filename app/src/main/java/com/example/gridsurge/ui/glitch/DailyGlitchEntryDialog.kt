package com.example.gridsurge.ui.glitch

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.ads.AdManager
import java.util.Locale
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.glitch.DailyGlitchCountdownManager
import com.example.gridsurge.game.glitch.DailyGlitchTier
import com.example.gridsurge.game.glitch.DailyGlitchUiState
import com.example.gridsurge.game.glitch.DailyLeaderboardEntry
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape

private val NeonGlitchGreen = Color(0xFF00FF66)
private val NeonGlitchRed = Color(0xFFFF0055)
private val NeonCyan = Color(0xFF00E5FF)
private val DarkModalBackdrop = Color(0xF203060C)
private val CardBackground = Color(0x66080E1A)
private val CardBorder = Color(0xFF1B2A42)

@Composable
fun DailyGlitchEntryDialog(
    uiState: DailyGlitchUiState,
    isNoAdsPurchased: Boolean = false,
    onLaunchMission: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdownText by remember { mutableStateOf(uiState.formattedTimeRemaining) }

    LaunchedEffect(Unit) {
        DailyGlitchCountdownManager.createTickerFlow().collect { (_, formatted) ->
            countdownText = formatted
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val bioPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bioPulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkModalBackdrop),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.88f)
            ) {
                // Vector Chamfer Frame Canvas
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val cut = 20.dp.toPx()

                    val framePath = Path().apply {
                        moveTo(cut, 0f)
                        lineTo(w - cut, 0f)
                        lineTo(w, cut)
                        lineTo(w, h - cut)
                        lineTo(w - cut, h)
                        lineTo(cut, h)
                        lineTo(0f, h - cut)
                        lineTo(0f, cut)
                        close()
                    }

                    drawPath(
                        path = framePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF07120D), Color(0xFF03070E))
                        )
                    )

                    drawPath(
                        path = framePath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonGlitchGreen.copy(alpha = bioPulseAlpha),
                                CardBorder,
                                NeonGlitchRed.copy(alpha = bioPulseAlpha * 0.7f)
                            )
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Corner Tech Accents
                    val cornerLen = 16.dp.toPx()
                    drawLine(NeonGlitchGreen, Offset(0f, cut + cornerLen), Offset(0f, cut), 3.dp.toPx())
                    drawLine(NeonGlitchGreen, Offset(0f, cut), Offset(cut, 0f), 3.dp.toPx())
                    drawLine(NeonGlitchGreen, Offset(cut, 0f), Offset(cut + cornerLen, 0f), 3.dp.toPx())

                    drawLine(NeonGlitchRed, Offset(w, h - cut - cornerLen), Offset(w, h - cut), 3.dp.toPx())
                    drawLine(NeonGlitchRed, Offset(w, h - cut), Offset(w - cut, h), 3.dp.toPx())
                    drawLine(NeonGlitchRed, Offset(w - cut, h), Offset(w - cut - cornerLen, h), 3.dp.toPx())
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // Header Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EVENT PROTOCOL // 24H",
                                color = NeonGlitchGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "DAILY GLITCH",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // UTC Countdown Tag
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RESET IN",
                                color = Color(0xFF7E8B9B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = countdownText,
                                color = NeonGlitchGreen.copy(alpha = bioPulseAlpha),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Seed & Rules Tag
                    Text(
                        text = "${uiState.seedDateFormatted} • EQUAL SEED MATRIX",
                        color = Color(0xFF8FA3BF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ticket Status Card
                    DailyGlitchTicketCard(
                        hasTicket = uiState.hasTicketAvailable,
                        score = uiState.userPersonalBestScore,
                        waves = uiState.userPersonalBestWaves,
                        userRank = uiState.userRank
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tiered Rewards Banner
                    TieredRewardsPreviewStrip()

                    Spacer(modifier = Modifier.height(14.dp))

                    // Leaderboard Preview Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECTOR LEADERBOARD",
                            color = Color(0xFF7E8B9B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "TOP 100 UPLINK",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Leaderboard Rows
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(CardBackground, CyberChamferShape)
                            .border(1.dp, CardBorder, CyberChamferShape)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(uiState.leaderboardPreview) { entry ->
                            LeaderboardRowItem(entry = entry)
                        }

                        if (uiState.userEntry != null && uiState.leaderboardPreview.none { it.isCurrentUser }) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "• • •",
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                                LeaderboardRowItem(entry = uiState.userEntry)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Controls
                    val context = LocalContext.current
                    val activity = context as? Activity

                    if (uiState.hasTicketAvailable) {
                        CyberActionButton(
                            text = "INITIALIZE PROTOCOL",
                            primaryColor = NeonGlitchGreen,
                            isPrimary = true,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onLaunchMission()
                            }
                        )
                    } else if (isNoAdsPurchased) {
                        CyberActionButton(
                            text = "BONUS RETRY [NO-ADS PASS ✓]",
                            primaryColor = NeonGlitchGreen,
                            isPrimary = true,
                            onClick = {
                                SfxManager.playSfx(SfxType.LEVEL_COMPLETE)
                                onLaunchMission()
                            }
                        )
                    } else {
                        CyberActionButton(
                            text = "RE-TRY SEED [WATCH AD]",
                            primaryColor = Color(0xFFFFD600),
                            isPrimary = true,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                if (activity != null) {
                                    AdManager.showRewardedAd(activity, isNoAdsPurchased) {
                                        onLaunchMission()
                                    }
                                } else {
                                    onLaunchMission()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    CyberActionButton(
                        text = "RETURN TO HUB",
                        primaryColor = Color(0xFF8A99AD),
                        isPrimary = false,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyGlitchTicketCard(
    hasTicket: Boolean,
    score: Long,
    waves: Int,
    userRank: Int?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    if (hasTicket) listOf(Color(0x2600FF66), Color(0x1000E5FF))
                    else listOf(Color(0x26FF0055), Color(0x10000000))
                ),
                shape = CyberChamferShape
            )
            .border(1.dp, if (hasTicket) Color(0x6600FF66) else Color(0x44FF0055), CyberChamferShape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (hasTicket) "1 FREE ACCESS PASS READY" else "DAILY ATTEMPT LOGGED",
                    color = if (hasTicket) NeonGlitchGreen else NeonGlitchRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (hasTicket) "Compete on identical seeds worldwide"
                    else "Current Run: Wave $waves • ${String.format(Locale.US, "%,d", score)} PTS",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (userRank != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "GLOBAL RANK",
                        color = Color(0xFF7E8B9B),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "#$userRank",
                        color = Color(0xFFFFD600),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun TieredRewardsPreviewStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x33000000), CyberChamferShape)
            .border(1.dp, CardBorder, CyberChamferShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RewardBadge(tier = "TOP 1%", reward = "500 ★ + TITLE", color = Color(0xFFFFD600))
        RewardBadge(tier = "TOP 5%", reward = "300 ★", color = Color(0xFFEA80FC))
        RewardBadge(tier = "TOP 20%", reward = "150 ★", color = Color(0xFF00E5FF))
    }
}

@Composable
private fun RewardBadge(tier: String, reward: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = tier,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = reward,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun LeaderboardRowItem(entry: DailyLeaderboardEntry) {
    val rowBackground = if (entry.isCurrentUser) Color(0x3300E5FF) else Color(0x1A0F172A)
    val rowBorder = if (entry.isCurrentUser) NeonCyan else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground, CyberChamferShape)
            .border(1.dp, rowBorder, CyberChamferShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format(Locale.US, "#%02d", entry.rank),
                color = when (entry.rank) {
                    1 -> Color(0xFFFFD600)
                    2 -> Color(0xFFE0E0E0)
                    3 -> Color(0xFFFF8A65)
                    else -> Color(0xFF7E8B9B)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = entry.callsign,
                color = if (entry.isCurrentUser) NeonCyan else Color.White,
                fontSize = 11.sp,
                fontWeight = if (entry.isCurrentUser) FontWeight.Black else FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "W${entry.wavesCleared}",
                color = NeonGlitchGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = String.format(Locale.US, "%,d", entry.score),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
