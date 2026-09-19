package com.example.gridsurge.ui.clash.components

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.clash.model.ClashArenaSnapshot
import com.example.gridsurge.game.clash.model.RivalCombatant
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun TacticalClashVisor(
    arena: ClashArenaSnapshot,
    rival: RivalCombatant,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLeading = arena.scoreDelta >= 0
    val deltaFormatted = String.format(Locale.US, "%,d", abs(arena.scoreDelta))
    val secondsClamped = arena.secondsRemaining.toInt().coerceAtLeast(0)

    val infiniteTransition = rememberInfiniteTransition(label = "clashVisorFx")

    val alertStrobe by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(260, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "strobe"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ==================== 1. GLADIATOR HUD CONSOLE ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xEE050A14))
                .border(
                    1.2.dp,
                    if (isLeading) Color(0xFF00E5FF).copy(alpha = 0.75f) else Color(0xFFFF0055).copy(alpha = 0.75f),
                    RoundedCornerShape(10.dp)
                )
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PLAYER HUD CARD (YOU)
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color(0xFF00E5FF))
                            )
                            Text(
                                text = "YOU // OPERATOR",
                                color = Color(0xFF00E5FF),
                                fontSize = 8.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "%,d", arena.playerScore),
                            color = if (arena.playerFeverActive) Color(0xFFFFD600) else Color.White,
                            fontSize = 17.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // MATCH CLOCK & DYNAMIC DELTA BADGE
                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", secondsClamped / 60, secondsClamped % 60),
                            color = if (secondsClamped <= 15) Color(0xFFFF0055).copy(alpha = alertStrobe) else Color.White,
                            fontSize = 15.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isLeading) Color(0x3300FF66) else Color(0x33FF0055))
                                .border(0.6.dp, if (isLeading) Color(0xFF00FF66) else Color(0xFFFF0055), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (isLeading) "+$deltaFormatted LEAD" else "-$deltaFormatted DEFICIT",
                                color = if (isLeading) Color(0xFF00FF66) else Color(0xFFFF0055),
                                fontSize = 7.5.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // RIVAL HUD CARD (OPPONENT)
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${rival.callsign} [${rival.tierTitle}]",
                                color = Color(0xFFFF0055),
                                fontSize = 8.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color(0xFFFF0055))
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "%,d", arena.rivalScore),
                            color = if (arena.rivalFeverActive) Color(0xFFFFD600) else Color(0xFFC4D2E6),
                            fontSize = 17.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Tactical Hex Pause Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // ==================== 2. KINETIC TUG-OF-WAR PLASMA GAUGE ====================
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val splitX = (w * arena.tugOfWarRatio).coerceIn(12f, w - 12f)

                        // Substrate Trench
                        drawRoundRect(
                            color = Color(0xFF0A121E),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        // Player Territory (Left Cyan Drive)
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF004466), Color(0xFF00E5FF))
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(splitX, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        // Rival Territory (Right Crimson Drive)
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFFF0055), Color(0xFF660022))
                            ),
                            topLeft = Offset(splitX, 0f),
                            size = Size(w - splitX, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )

                        // High-Voltage Plasma Spark at Collision Boundary
                        val sparkJitterX = splitX + (Random.nextFloat() - 0.5f) * 4.dp.toPx()
                        drawLine(
                            color = Color.White,
                            start = Offset(sparkJitterX, -2.dp.toPx()),
                            end = Offset(sparkJitterX, h + 2.dp.toPx()),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Electric Plasma Flare Discharge
                        drawCircle(
                            color = Color(0xFF00FFFF).copy(alpha = 0.85f),
                            radius = 5.dp.toPx(),
                            center = Offset(sparkJitterX, h / 2f)
                        )
                    }

                    // Tactical Status Sub-Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (arena.playerFeverActive) "★ OVERDRIVE ENGAGED [3X] ★" else "${arena.playerPpm} PPM PACE",
                            color = if (arena.playerFeverActive) Color(0xFFFFD600) else Color(0xFF556980),
                            fontSize = 7.5.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (arena.rivalFeverActive) "★ RIVAL IN OVERDRIVE [3X] ★" else "${arena.rivalPpm} PPM PACING",
                            color = if (arena.rivalFeverActive) Color(0xFFFF0055).copy(alpha = alertStrobe) else Color(0xFF556980),
                            fontSize = 7.5.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
