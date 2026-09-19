package com.example.gridsurge.ui.blitz.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.blitz.model.BlitzPhase
import com.example.gridsurge.game.blitz.model.TimeBlitzState
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale

@Composable
fun TacticalBlitzVisor(
    state: TimeBlitzState,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = state.phase == BlitzPhase.CRITICAL_CHRONO
    val isFever = state.isFeverActive

    val infiniteTransition = rememberInfiniteTransition(label = "blitzVisorFx")

    // Critical time alarm strobe
    val strobeAlpha by infiniteTransition.animateFloat(
        initialValue = if (isCritical) 0.35f else 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCritical) 240 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe"
    )

    val timerColor = when {
        isCritical -> Color(0xFFFF0055).copy(alpha = strobeAlpha)
        isFever -> Color(0xFFFFD600)
        else -> Color(0xFF00FF66)
    }

    val headerBorderColor = when {
        isFever -> Color(0xFFFFD600)
        isCritical -> Color(0xFFFF0055)
        else -> Color(0xFF00E5FF).copy(alpha = 0.65f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Upper Telemetry Chassis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xEE050A14))
                .border(1.2.dp, headerBorderColor, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Score Tile
                    BlitzDataTile(
                        label = "SCORE",
                        value = String.format(Locale.US, "%,d", state.score),
                        valueColor = if (isFever) Color(0xFFFFD600) else Color.White,
                        modifier = Modifier.weight(1.1f)
                    )

                    // 2. Chrono Timer Tile
                    val seconds = state.secondsRemaining.toInt()
                    BlitzDataTile(
                        label = if (isCritical) "CHRONO CRITICAL" else "TIME REMAINING",
                        value = String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60),
                        valueColor = timerColor,
                        modifier = Modifier.weight(1.0f)
                    )

                    // 3. PPM Cadence Tile
                    BlitzDataTile(
                        label = "PACE",
                        value = "${state.piecesPerMinute} PPM",
                        valueColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(0.9f)
                    )

                    // Pause Trigger
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0C1626))
                            .border(1.dp, Color(0xFF1E3250), RoundedCornerShape(6.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onPauseClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Fever / Overdrive Bar
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFever) "★ FEVER OVERDRIVE // 3X MULTIPLIER ACTIVE ★" else "OVERDRIVE CHARGE",
                            color = if (isFever) Color(0xFFFFD600) else Color(0xFF556980),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${(state.feverMeter * 100).toInt()}%",
                            color = if (isFever) Color(0xFFFFD600) else Color.White,
                            fontSize = 9.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val progress = state.feverMeter.coerceIn(0f, 1f)

                        // Base track
                        drawRoundRect(
                            color = Color(0xFF0B1422),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        // Active gradient fill
                        val fillBrush = if (isFever) {
                            Brush.horizontalGradient(listOf(Color(0xFFFF1744), Color(0xFFFFD600)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFF007799), Color(0xFF00E5FF)))
                        }

                        drawRoundRect(
                            brush = fillBrush,
                            size = Size(w * progress, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlitzDataTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC08101E))
            .border(0.8.dp, Color(0xFF142236), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                color = Color(0xFF556980),
                fontSize = 7.5.sp,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 13.5.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black
            )
        }
    }
}
