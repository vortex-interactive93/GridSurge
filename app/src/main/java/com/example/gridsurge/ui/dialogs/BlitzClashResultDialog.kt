package com.example.gridsurge.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape

@Composable
fun BlitzClashResultDialog(
    isWinner: Boolean,
    playerScore: Long,
    rivalScore: Long,
    starsEarned: Int,
    ratingDelta: Int,
    maxCombo: Int,
    linesCleared: Int,
    replayData: com.example.gridsurge.game.replay.MatchReplayData? = null,
    onWatchReplay: () -> Unit,
    onRematch: () -> Unit,
    onExit: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val neonColor = if (isWinner) Color(0xFF00FF66) else Color(0xFFFF0055)
    val headerText = if (isWinner) "CLASH VICTORY" else "RIVAL OVERCLOCKED"

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
                shape = CyberChamferShape,
                color = Color(0xFF0A0F1D),
                border = BorderStroke(2.dp, neonColor.copy(alpha = glowAlpha))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = headerText,
                        color = neonColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Score Comparison
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScoreCard("YOU", playerScore, Color(0xFF00E5FF))
                        Text("VS", color = Color.Gray, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        ScoreCard("RIVAL", rivalScore, Color(0xFFFF0055))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reward Section
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0x3300E5FF), shape = RoundedCornerShape(8.dp)).padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DATA FRAGS RECOVERED: +$starsEarned STARS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RATING ADJUSTMENT: ${if (ratingDelta >= 0) "+" else ""}$ratingDelta RP",
                            color = if (ratingDelta >= 0) Color(0xFF00FF66) else Color(0xFFFF0055),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    CyberActionButton(
                        text = "NEW CHALLENGE",
                        primaryColor = Color(0xFF00E5FF),
                        isPrimary = true,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onRematch()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (replayData != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                CyberActionButton(
                                    text = "WATCH REPLAY",
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
                                    primaryColor = Color(0xFF00FF66),
                                    isPrimary = false,
                                    outlineBrush = Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00FF66))),
                                    onClick = {
                                        SfxManager.playSfx(SfxType.UI_CONFIRM)
                                        com.example.gridsurge.analytics.GridSurgeAnalytics.logShareEvent("CLASH_CARD")
                                        com.example.gridsurge.game.share.CyberShareCardGenerator.generateAndShareClashCard(
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
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    CyberActionButton(
                        text = "ABORT TO HUB",
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

@Composable
private fun ScoreCard(label: String, score: Long, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(
            text = String.format("%,d", score),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}
