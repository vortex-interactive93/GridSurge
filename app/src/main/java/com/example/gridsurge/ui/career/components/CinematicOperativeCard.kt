package com.example.gridsurge.ui.career.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.career.model.OperativeDossierState
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun CinematicOperativeCard(
    state: OperativeDossierState,
    modifier: Modifier = Modifier
) {
    val clearanceColor = state.clearance.primaryColor

    // Animated XP Fill Engine
    val xpProgressAnim = remember { Animatable(0f) }
    LaunchedEffect(state.currentXp) {
        xpProgressAnim.snapTo(0f)
        xpProgressAnim.animateTo(
            targetValue = (state.currentXp.toFloat() / state.xpForNextLevel).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1200, delayMillis = 250, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .cyberBorderGlow(
                colors = listOf(clearanceColor, Color.Transparent),
                cornerRadius = 14.dp
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF20F192C), Color(0xFD060A13))
                )
            )
            .border(1.2.dp, clearanceColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Clearance Band
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(clearanceColor.copy(alpha = 0.2f))
                            .border(0.5.dp, clearanceColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = state.clearance.badgeLabel,
                            color = clearanceColor,
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "UID: ${state.uidTag}",
                        color = Color(0xFF8FA3BF),
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD500F9).copy(alpha = 0.2f))
                        .border(0.8.dp, Color(0xFFD500F9), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COMBAT: ${state.combatEfficiencyGrade}",
                        color = Color(0xFFD500F9),
                        fontSize = 9.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Core Profile Row with Live Holographic Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HolographicAvatarCore(
                    accentColor = clearanceColor,
                    size = 64.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.callsign,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = state.clearance.title,
                        color = clearanceColor,
                        fontSize = 9.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "LEVEL 0${state.currentLevel} OPERATIVE // ACTIVE PROTOCOL",
                        color = Color(0xFF556980),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily
                    )
                }
            }

            // Dynamic Segmented XP Meter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PROGRESSION TO NEXT CLEARANCE",
                        color = Color(0xFF8FA3BF),
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.currentXp} / ${state.xpForNextLevel} XP",
                        color = clearanceColor,
                        fontSize = 9.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val totalBlocks = 20
                    val gap = 3.dp.toPx()
                    val blockW = (w - (gap * (totalBlocks - 1))) / totalBlocks
                    val activeRatio = xpProgressAnim.value
                    val activeBlocks = (activeRatio * totalBlocks).toInt()

                    for (i in 0 until totalBlocks) {
                        val x = i * (blockW + gap)
                        val isActive = i < activeBlocks
                        val isLeading = i == activeBlocks - 1

                        drawRoundRect(
                            color = when {
                                isLeading -> Color.White
                                isActive -> clearanceColor
                                else -> Color(0xFF131D2E)
                            },
                            topLeft = Offset(x, 0f),
                            size = Size(blockW, h),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
