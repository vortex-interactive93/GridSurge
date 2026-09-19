package com.example.gridsurge.ui.hub.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.monetization.model.FirewallJammerState
import com.example.gridsurge.monetization.model.JammerStatus
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun FirewallJammerCard(
    state: FirewallJammerState,
    onWatchAdToExtend: () -> Unit,
    onVipUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "jammerSheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "sheen"
    )

    // Dynamic State Colors
    val statusColor = when (state.status) {
        JammerStatus.PERMANENT_VIP -> Color(0xFF00FF66)  // Emerald VIP
        JammerStatus.MAX_CAPACITY -> Color(0xFF00E5FF)   // Cyan Maxed
        JammerStatus.ACTIVE_STABLE -> Color(0xFF00FF66)  // Green Protected
        JammerStatus.ACTIVE_EXPIRING -> Color(0xFFFFD600)// Amber Warning
        JammerStatus.OFFLINE -> Color(0xFF556980)        // Inactive Carbon
    }

    val isStackAllowed = state.status != JammerStatus.MAX_CAPACITY && 
                         state.status != JammerStatus.PERMANENT_VIP && 
                         !state.isAdBuffering

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xE6050A14))
            .border(1.2.dp, statusColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Header: System Label & Live Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (state.status) {
                            JammerStatus.PERMANENT_VIP -> "NEURAL FIREWALL // VIP OVERRIDE"
                            JammerStatus.OFFLINE -> "FIREWALL JAMMER // INACTIVE"
                            else -> "FIREWALL JAMMER // ACTIVE SHIELD"
                        },
                        color = statusColor,
                        fontSize = 8.5.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Digital Chrono Countdown
                Text(
                    text = state.formattedTime,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }

            // Segmented Fuel / Capacity Track
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            ) {
                val w = size.width
                val h = size.height

                // Substrate Track
                drawRoundRect(
                    color = Color(0xFF0A121E),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Fill Progress
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(statusColor.copy(alpha = 0.5f), statusColor)
                    ),
                    size = Size(w * state.progressRatio, h),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Stacking Button: Watch Ad -> +5m
                Box(
                    modifier = Modifier
                        .weight(1.6f)
                        .height(38.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isStackAllowed) {
                                Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF007799)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF131D2E), Color(0xFF0B1422)))
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = isStackAllowed
                        ) {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onWatchAdToExtend()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isStackAllowed) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val w = size.width
                            val h = size.height
                            val sheenX = w * sheenProgress
                            val sheenWidth = w * 0.35f

                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent),
                                    start = Offset(sheenX, 0f),
                                    end = Offset(sheenX + sheenWidth, h)
                                ),
                                size = Size(w, h)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = if (isStackAllowed) Color(0xFF03070E) else Color(0xFF556980),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when {
                                state.status == JammerStatus.PERMANENT_VIP -> "PERMANENT ACTIVE"
                                state.status == JammerStatus.MAX_CAPACITY -> "BUFFER FULL (60M)"
                                state.isAdBuffering -> "LOADING LINK..."
                                else -> "EXTEND +5 MIN [AD]"
                            },
                            color = if (isStackAllowed) Color(0xFF03070E) else Color(0xFF556980),
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Secondary CTA: Upgrade to Permanent VIP
                if (!state.isPermanentNoAds) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2200FF66))
                            .border(0.8.dp, Color(0xFF00FF66).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .clickable(onClick = onVipUpgradeClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GET VIP NO-ADS",
                            color = Color(0xFF00FF66),
                            fontSize = 8.5.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
