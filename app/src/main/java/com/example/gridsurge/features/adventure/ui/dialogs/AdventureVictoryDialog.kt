package com.example.gridsurge.features.adventure.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.features.adventure.model.StarEvaluationResult
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.CyberChamferShape
import kotlinx.coroutines.delay

@Composable
fun AdventureVictoryDialog(
    levelNumber: Int,
    score: Long,
    evaluationResult: StarEvaluationResult,
    finalTimeSeconds: Int,
    isSectorBoss: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onReturnToMap: () -> Unit
) {
    var showStars by remember { mutableStateOf(false) }
    var showChecklist by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200L)
        showStars = true
        delay(400L)
        showChecklist = true
        SfxManager.playSfx(SfxType.SNAP_TICK)
    }

    Dialog(
        onDismissRequest = onReturnToMap,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(
                        brush = Brush.verticalGradient(listOf(Color(0xFF0A1322), Color(0xFF030712))),
                        shape = CyberChamferShape
                    )
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
            ) {
                // Top-Right Close Button
                IconButton(
                    onClick = onReturnToMap,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x2B00E5FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Return to Map",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSectorBoss) "SECTOR OVERLORD NEUTRALIZED" else "OPERATION SUCCESSFUL",
                        color = Color(0xFF00FF66),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "STAGE $levelNumber SYNCHRONIZED",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Animated Stars Header
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        val starsTotal = evaluationResult.totalStars
                        for (i in 1..3) {
                            val isLit = i <= starsTotal
                            Text(
                                text = if (isLit) "★" else "☆",
                                color = if (isLit) Color(0xFFFFD700) else Color(0xFF263859),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // 3-Directive Checklist Box
                    AnimatedVisibility(
                        visible = showChecklist,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF07101C))
                                .border(1.dp, Color(0xFF16253B), RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DirectiveCheckItem(
                                isSecured = evaluationResult.star1Secured,
                                title = evaluationResult.star1Title,
                                detail = evaluationResult.star1Detail
                            )
                            DirectiveCheckItem(
                                isSecured = evaluationResult.star2Secured,
                                title = evaluationResult.star2Title,
                                detail = evaluationResult.star2Detail
                            )
                            DirectiveCheckItem(
                                isSecured = evaluationResult.star3Secured,
                                title = evaluationResult.star3Title,
                                detail = evaluationResult.star3Detail
                            )
                        }
                    }

                    // Score & Telemetry Meta
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FINAL SCORE", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("$score PTS", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL DURATION", color = Color(0xFF5C8599), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            val mins = finalTimeSeconds / 60
                            val secs = finalTimeSeconds % 60
                            Text(String.format("%02d:%02d", mins, secs), color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action Buttons
                    ButtonRow(
                        isSectorBoss = isSectorBoss,
                        onNextLevel = onNextLevel,
                        onReplay = onReplay,
                        onReturnToMap = onReturnToMap
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectiveCheckItem(
    isSecured: Boolean,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (isSecured) "[✔]" else "[ ]",
                color = if (isSecured) Color(0xFF00FF66) else Color(0xFF5C8599),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = title,
                    color = if (isSecured) Color.White else Color(0xFF8FA3BF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detail,
                    color = if (isSecured) Color(0xFF00E5FF) else Color(0xFF5C8599),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Text(
            text = "★",
            color = if (isSecured) Color(0xFFFFD700) else Color(0xFF1E2D4A),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ButtonRow(
    isSectorBoss: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onReturnToMap: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF00E5FF))
                .clickable {
                    SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                    onNextLevel()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSectorBoss) "CLAIM RELIC & ADVANCE" else "NEXT LEVEL",
                color = Color(0xFF030712),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0A1322))
                .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(4.dp))
                .clickable {
                    SfxManager.playSfx(SfxType.SNAP_TICK)
                    onReplay()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "REPLAY STAGE",
                color = Color(0xFF8FA3BF),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "RETURN TO HUB MAP",
            color = Color(0xFF5C8599),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onReturnToMap() }
        )
    }
}
