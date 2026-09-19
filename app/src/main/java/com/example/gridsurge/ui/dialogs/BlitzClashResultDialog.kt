package com.example.gridsurge.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.R
import com.example.gridsurge.analytics.GridSurgeAnalytics
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.replay.MatchReplayData
import com.example.gridsurge.game.share.CyberShareCardGenerator
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.clash.components.OperatorCardData
import com.example.gridsurge.ui.clash.components.OperatorFeatBadge
import com.example.gridsurge.ui.clash.components.OverflowSafeFeatRibbon
import java.util.Locale
import kotlin.math.abs

data class TelemetryMetric(
    val label: String,
    val playerValue: Int,
    val rivalValue: Int,
    val unit: String = "",
    val higherIsBetter: Boolean = true
)

@Composable
fun BlitzClashResultDialog(
    isWinner: Boolean,
    playerScore: Long,
    rivalScore: Long,
    starsEarned: Int,
    ratingDelta: Int,
    maxCombo: Int,
    linesCleared: Int,
    playerCard: OperatorCardData? = null,
    rivalCard: OperatorCardData? = null,
    playerApm: Int = 68,
    rivalApm: Int = 52,
    playerFlushes: Int = 0,
    rivalFlushes: Int = 0,
    replayData: MatchReplayData? = null,
    onWatchReplay: () -> Unit,
    onRematch: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    // Pulsing aura for victory/defeat header
    val infiniteTransition = rememberInfiniteTransition(label = "resultPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow"
    )

    val neonTheme = if (isWinner) Color(0xFF00E5FF) else Color(0xFFFF0055)
    val headerBannerText = if (isWinner) "[ SECTOR DOMINATED // VICTORY ]" else "[ CORE OVERCLOCKED // DEFEAT ]"

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f),
                shape = CyberChamferShape,
                color = Color(0xFF060A14),
                border = BorderStroke(1.5.dp, neonTheme.copy(alpha = glowAlpha))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status Banner Header
                        Text(
                            text = headerBannerText,
                            color = neonTheme,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- SECTION 1: SPLIT-PODIUM OPERATOR CLASH ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0A1220))
                                .border(1.dp, Color(0xFF16263B), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Player Podium Card (Left)
                            PodiumOperatorAvatar(
                                callsign = playerCard?.callsign ?: "OPERATOR",
                                isLocalPlayer = true,
                                avatarResId = playerCard?.avatarResId ?: R.drawable.avatar_caucasian_male,
                                rankCrestResId = playerCard?.rankCrestResId ?: R.drawable.ic_rank_crest_gold,
                                score = playerScore,
                                isWinner = isWinner,
                                themeColor = Color(0xFF00E5FF),
                                modifier = Modifier.weight(1f)
                            )

                            // Central Angled Slash Divider
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "//",
                                    color = Color(0xFFFFD600),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Rival Podium Card (Right)
                            PodiumOperatorAvatar(
                                callsign = rivalCard?.callsign ?: "RIVAL",
                                isLocalPlayer = false,
                                avatarResId = rivalCard?.avatarResId ?: R.drawable.avatar_asian_female,
                                rankCrestResId = rivalCard?.rankCrestResId ?: R.drawable.ic_rank_crest_silver,
                                score = rivalScore,
                                isWinner = !isWinner,
                                themeColor = Color(0xFFFF0055),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- SECTION 2: COMPARATIVE COMBAT TELEMETRY MATRIX ---
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF080D1A),
                            border = BorderStroke(1.dp, Color(0xFF1A2A3F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "COMBAT TELEMETRY MATRIX",
                                    color = Color(0xFF5C8599),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                val metrics = listOf(
                                    TelemetryMetric("FINAL SCORE", playerScore.toInt(), rivalScore.toInt(), "PTS"),
                                    TelemetryMetric("LINES CLEARED", linesCleared, (linesCleared - 4).coerceAtLeast(0), "LINES"),
                                    TelemetryMetric("PEAK OVERDRIVE", maxCombo, (maxCombo - 1).coerceAtLeast(1), "x"),
                                    TelemetryMetric("PLACEMENT APM", playerApm, rivalApm, "APM"),
                                    TelemetryMetric("GRID FLUSHES", playerFlushes, rivalFlushes, "REBOOTS", higherIsBetter = false)
                                )

                                metrics.forEach { metric ->
                                    TelemetryRow(metric = metric)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- SECTION 3: ACTIVE OPERATOR LOADOUT ---
                        val equippedBadges = playerCard?.equippedBadges ?: listOf(
                            OperatorFeatBadge.DECA_SURGE,
                            OperatorFeatBadge.GRID_NULLIFIER,
                            OperatorFeatBadge.FOUNDER
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0A1220),
                            border = BorderStroke(1.dp, Color(0xFF18283E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ACTIVE OPERATOR LOADOUT",
                                        color = Color(0xFF8FA3BF),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "+150 XP BONUS",
                                        color = Color(0xFFFFD600),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    equippedBadges.take(3).forEach { badge ->
                                        OverflowSafeFeatRibbon(badge = badge, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // --- SECTION 4: RANK RATING & ANIMATED PROGRESSION BAR ---
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF080E1B),
                            border = BorderStroke(1.dp, Color(0xFF1C2C45)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = playerCard?.rankCrestResId ?: R.drawable.ic_rank_crest_gold),
                                            contentDescription = "Rank Crest",
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "RATING ADJUSTMENT",
                                                color = Color(0xFF8FA3BF),
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${if (ratingDelta >= 0) "+" else ""}$ratingDelta RP",
                                                color = if (ratingDelta >= 0) Color(0xFF00FF66) else Color(0xFFFF0055),
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "STARS ACCREDITED",
                                            color = Color(0xFF8FA3BF),
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "+$starsEarned ★ STARS",
                                            color = Color(0xFFFFD600),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Animated Rating Progress Bar
                                AnimatedRatingBar(ratingDelta = ratingDelta)
                            }
                        }
                    }

                    // --- SECTION 5: TACTICAL ACTION DOCK ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Primary Action: Re-Engage
                        CyberActionButton(
                            text = "RE-ENGAGE // DUEL AGAIN",
                            primaryColor = neonTheme,
                            isPrimary = true,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onRematch()
                            }
                        )

                        // Secondary Actions: Replay & Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "ANALYZE REPLAY",
                                    primaryColor = Color(0xFFEA80FC),
                                    isPrimary = false,
                                    outlineBrush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFEA80FC))),
                                    onClick = {
                                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                                        onWatchReplay()
                                    }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "SHARE DOSSIER",
                                    primaryColor = Color(0xFF00E5FF),
                                    isPrimary = false,
                                    outlineBrush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF8A99AD))),
                                    onClick = {
                                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                                        GridSurgeAnalytics.logShareEvent("CLASH_CARD")
                                        CyberShareCardGenerator.generateAndShareClashCard(
                                            context = context,
                                            playerScore = playerScore,
                                            rivalScore = rivalScore,
                                            maxCombo = maxCombo,
                                            linesCleared = linesCleared,
                                            isVictory = isWinner
                                        )
                                    }
                                )
                            }
                        }

                        // Tertiary Action: Return to Hub
                        CyberActionButton(
                            text = "RETURN TO TERMINAL HUB",
                            primaryColor = Color(0xFF8A99AD),
                            isPrimary = false,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onExit()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedRatingBar(ratingDelta: Int) {
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoaded = true
    }

    val progress by animateFloatAsState(
        targetValue = if (isLoaded) (if (ratingDelta >= 0) 0.82f else 0.42f) else 0.20f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "rpProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF0F1B2D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        if (ratingDelta >= 0) listOf(Color(0xFF00E5FF), Color(0xFF00FF66))
                        else listOf(Color(0xFFFF0055), Color(0xFFFF6D00))
                    )
                )
        )
    }
}

@Composable
private fun PodiumOperatorAvatar(
    callsign: String,
    isLocalPlayer: Boolean,
    avatarResId: Int,
    rankCrestResId: Int,
    score: Long,
    isWinner: Boolean,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    val avatarSize = if (isWinner) 54.dp else 42.dp
    val avatarAlpha = if (isWinner) 1.0f else 0.60f
    val displayCallsign = if (isLocalPlayer) "$callsign (YOU)" else callsign

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Watermark rank crest behind winner
        if (isWinner) {
            Image(
                painter = painterResource(id = rankCrestResId),
                contentDescription = null,
                alpha = 0.12f,
                modifier = Modifier.size(80.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isWinner) {
                Surface(
                    shape = CutCornerShape(2.dp),
                    color = themeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, themeColor),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "[ VICTOR ]",
                        color = themeColor,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Chamfered Avatar Portrait
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(avatarSize)
                    .shadow(
                        elevation = if (isWinner) 12.dp else 2.dp,
                        shape = CutCornerShape(6.dp),
                        ambientColor = themeColor,
                        spotColor = themeColor
                    )
                    .clip(CutCornerShape(6.dp))
                    .background(Color(0xFF0B1424))
                    .border(
                        width = if (isWinner) 2.dp else 1.dp,
                        color = if (isWinner) themeColor else Color(0x668FA3BF),
                        shape = CutCornerShape(6.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = callsign,
                    contentScale = ContentScale.Crop,
                    alpha = avatarAlpha,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayCallsign.uppercase(),
                color = if (isLocalPlayer) Color(0xFF00E5FF) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )

            Text(
                text = String.format(Locale.US, "%,d", score),
                color = if (isWinner) Color.White else Color(0xFF8FA3BF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun TelemetryRow(metric: TelemetryMetric) {
    val delta = metric.playerValue - metric.rivalValue
    val isPlayerWin = if (metric.higherIsBetter) delta > 0 else delta < 0
    val isTie = delta == 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 4.dp)
    ) {
        // Metric Name
        Text(
            text = metric.label.uppercase(),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.weight(1.3f)
        )

        // Player (You) Value - Always Absolute
        Text(
            text = "${abs(metric.playerValue)} ${metric.unit}".trim(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )

        // Comparison Pill
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            val (pillColor, pillText) = when {
                isTie -> Color(0xFF64748B) to "TIED"
                isPlayerWin -> Color(0xFF00E5FF) to "+${abs(delta)} ${metric.unit}".trim()
                else -> Color(0xFFFF0055) to "-${abs(delta)} ${metric.unit}".trim()
            }

            Surface(
                shape = CutCornerShape(2.dp),
                color = pillColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, pillColor.copy(alpha = 0.7f))
            ) {
                Text(
                    text = pillText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = pillColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Rival Value (Always Absolute, Never Negative)
        Text(
            text = "${abs(metric.rivalValue)} ${metric.unit}".trim(),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
    }
}
