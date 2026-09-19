package com.example.gridsurge.ui.clash.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.clash.engine.RivalMatchmakingRegistry
import com.example.gridsurge.game.clash.model.RivalCombatant
import com.example.gridsurge.game.clash.network.model.LiveMatchmakingPhase
import com.example.gridsurge.ui.dialogs.CyberAvatarRegistry
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveClashMatchmakingTerminal(
    phase: LiveMatchmakingPhase,
    elapsedSeconds: Int,
    searchMmrWindow: Int,
    playerMmr: Int,
    matchedRival: RivalCombatant?,
    countdownRemainingSec: Int,
    onCancelQueue: () -> Unit,
    localCallsign: String = "OPERATOR_01",
    playerLevel: Int = 32,
    playerWinLoss: String = "62% W/L",
    localAvatarResId: Int = R.drawable.avatar_caucasian_male,
    localEquippedBadges: List<OperatorFeatBadge> = listOf(
        OperatorFeatBadge.DECA_SURGE,
        OperatorFeatBadge.GRID_NULLIFIER,
        OperatorFeatBadge.FOUNDER
    ),
    modifier: Modifier = Modifier
) {
    if (phase == LiveMatchmakingPhase.STAGING_COUNTDOWN && matchedRival != null) {
        val playerTier = RivalMatchmakingRegistry.generateOpponent(playerMmr).tierTitle
        val playerCrestResId = when {
            playerMmr >= 2000 -> R.drawable.ic_rank_crest_gold
            playerMmr >= 1500 -> R.drawable.ic_rank_crest_silver
            else -> R.drawable.ic_rank_crest_bronze
        }
        val playerCard = OperatorCardData(
            callsign = localCallsign.ifBlank { "OPERATOR_01" },
            level = playerLevel,
            tierTitle = playerTier,
            mmr = playerMmr,
            winLossRatio = playerWinLoss,
            equippedBadges = localEquippedBadges,
            isLocalPlayer = true,
            avatarResId = localAvatarResId,
            rankCrestResId = playerCrestResId
        )

        val rivalBadges = remember(matchedRival.equippedBadgeIds) {
            val ids = matchedRival.equippedBadgeIds
            if (ids.isNotEmpty()) {
                ids.map { OperatorFeatBadge.fromId(it) }.distinct()
            } else {
                listOf(OperatorFeatBadge.DECA_SURGE, OperatorFeatBadge.GRID_NULLIFIER, OperatorFeatBadge.FOUNDER)
            }
        }

        val rivalLevel = (matchedRival.currentMmr / 38).coerceIn(14, 99)
        val rivalWinPct = (52 + (matchedRival.currentMmr % 18)).coerceIn(48, 89)
        val rivalAvatarPreset = remember(matchedRival.avatarKey) {
            if (matchedRival.avatarKey.isNotBlank()) {
                CyberAvatarRegistry.getPresetById(matchedRival.avatarKey)
            } else {
                CyberAvatarRegistry.PRESETS.first()
            }
        }
        val rivalCrestResId = when {
            matchedRival.currentMmr >= 2000 -> R.drawable.ic_rank_crest_gold
            matchedRival.currentMmr >= 1500 -> R.drawable.ic_rank_crest_silver
            else -> R.drawable.ic_rank_crest_bronze
        }

        val rivalCard = OperatorCardData(
            callsign = matchedRival.callsign,
            level = rivalLevel,
            tierTitle = matchedRival.tierTitle,
            mmr = matchedRival.currentMmr,
            winLossRatio = "$rivalWinPct% W/L",
            equippedBadges = rivalBadges,
            isLocalPlayer = false,
            avatarResId = rivalAvatarPreset.iconRes,
            rankCrestResId = rivalCrestResId
        )

        BlitzClashVersusSplash(
            playerCard = playerCard,
            rivalCard = rivalCard,
            countdownRemainingSec = countdownRemainingSec,
            modifier = modifier
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radarFx")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF502050A))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==================== 1. RADAR SCANNER DECK ====================
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val maxRadius = w / 2f

                    // Concentric Radar Rings
                    for (i in 1..4) {
                        val r = (maxRadius / 4f) * i
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            radius = r,
                            center = center,
                            style = Stroke(1.dp.toPx())
                        )
                    }

                    // Crosshair Graticules
                    drawLine(Color(0xFF00E5FF).copy(alpha = 0.2f), Offset(center.x, 0f), Offset(center.x, h), 1.dp.toPx())
                    drawLine(Color(0xFF00E5FF).copy(alpha = 0.2f), Offset(0f, center.y), Offset(w, center.y), 1.dp.toPx())

                    // 360-Degree Sweep Cone
                    if (phase == LiveMatchmakingPhase.SEARCHING_QUEUE) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to Color.Transparent,
                                0.75f to Color.Transparent,
                                1.0f to Color(0xFF00E5FF).copy(alpha = 0.45f)
                            ),
                            startAngle = sweepAngle - 90f,
                            sweepAngle = 90f,
                            useCenter = true,
                            topLeft = Offset.Zero,
                            size = size
                        )

                        // Sweeping Leading Edge Line
                        val rad = Math.toRadians(sweepAngle.toDouble())
                        val endX = center.x + (cos(rad) * maxRadius).toFloat()
                        val endY = center.y + (sin(rad) * maxRadius).toFloat()
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Inner Status Pip
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC07101E))
                        .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // ==================== 2. TELEMETRY & MATCHMAKING INFO ====================
            if (phase == LiveMatchmakingPhase.SEARCHING_QUEUE) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SCANNING GLOBAL SECTOR...",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )

                    val m = elapsedSeconds / 60
                    val s = elapsedSeconds % 60
                    Text(
                        text = String.format("ELAPSED: %02d:%02d", m, s),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )

                    // Active Search Bracket
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0A1424))
                            .border(0.8.dp, Color(0xFF1E3250), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MMR TARGET: [${playerMmr - searchMmrWindow} - ${playerMmr + searchMmrWindow}] (±${searchMmrWindow})",
                            color = Color(0xFF8FA3BF),
                            fontSize = 8.5.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Abort Queue CTA
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22FF0055))
                        .border(1.dp, Color(0xFFFF0055).copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .clickable {
                            SfxManager.playSfx(SfxType.UI_BACK)
                            onCancelQueue()
                        }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF0055), modifier = Modifier.size(14.dp))
                        Text(
                            text = "ABORT SEARCH",
                            color = Color(0xFFFF0055),
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
