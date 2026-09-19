package com.example.gridsurge.ui.glitch

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.glitch.model.DailyGlitchDebriefState
import com.example.gridsurge.ui.CyberActionButton
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import java.util.Locale

@Composable
fun DailyGlitchResultDialog(
    debrief: DailyGlitchDebriefState,
    onViewLeaderboard: () -> Unit,
    onReturnToHub: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "debriefFx")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    Dialog(
        onDismissRequest = onReturnToHub,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF203060C)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .cyberBorderGlow(
                        colors = listOf(Color(0xFF00FF66), Color(0xFFFF0055)),
                        cornerRadius = 16.dp
                    )
                    .clip(CyberChamferShape)
                    .background(Color(0xFF070D18))
                    .border(1.5.dp, Color(0xFF00FF66), CyberChamferShape)
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Tag
                    Text(
                        text = "ANOMALY PROTOCOL // DEBRIEF",
                        color = Color(0xFF00FF66),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "CONTAINMENT COMPLETE",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    // Standing Badge Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0D1626))
                            .border(1.dp, Color(0xFF1E2F4A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GLOBAL STANDING",
                                color = Color(0xFF7E8B9B),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (debrief.globalRank != null) "#${debrief.globalRank}" else "PENDING",
                                color = Color(0xFFFFD600),
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BRACKET TIER",
                                color = Color(0xFF7E8B9B),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = debrief.percentileTier,
                                color = Color(0xFF00E5FF),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Metrics Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "FINAL SCORE",
                            value = String.format(Locale.US, "%,d", debrief.finalScore),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "CATALYSTS PURGED",
                            value = "${debrief.catalystsPurged}",
                            color = Color(0xFF00FF66),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Star Bounty Reward Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0A121F))
                            .border(0.8.dp, Color(0xFF1C2C45), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECTOR BOUNTY AWARD:",
                            color = Color(0xFF8FA3BF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+${debrief.starsAwarded} ★ STARS",
                            color = Color(0xFFFFD600),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Primary Action: View Leaderboard
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00FF66), Color(0xFF00C853))
                                )
                            )
                            .clickable {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onViewLeaderboard()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val w = size.width
                            val h = size.height
                            val sheenX = w * sheenProgress
                            val sheenWidth = w * 0.35f

                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    start = Offset(sheenX, 0f),
                                    end = Offset(sheenX + sheenWidth, h)
                                ),
                                size = Size(w, h)
                            )
                        }

                        Text(
                            text = "VIEW 24H LEADERBOARD",
                            color = Color(0xFF03070E),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Secondary Action: Return to Hub
                    CyberActionButton(
                        text = "RETURN TO COMMAND HUB",
                        primaryColor = Color(0xFF8A99AD),
                        isPrimary = false,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onReturnToHub()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x990A111E))
            .border(0.8.dp, Color(0xFF172436), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                color = Color(0xFF556980),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = color,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }
    }
}
