package com.example.gridsurge.ui.operations.components

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
import androidx.compose.material.icons.filled.Star
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
import com.example.gridsurge.operations.model.DirectiveStatus
import com.example.gridsurge.operations.model.OperationDirective
import com.example.gridsurge.ui.components.FloatingRewardAnchor
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun DirectiveCard(
    directive: OperationDirective,
    onDeploy: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClaimable = directive.status == DirectiveStatus.READY_TO_CLAIM
    val isClaimed = directive.status == DirectiveStatus.CLAIMED
    val accentColor = directive.difficulty.primaryColor

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "directiveScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    var claimToastKey by remember { mutableStateOf<Long?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isClaimable) {
                    Modifier.cyberBorderGlow(
                        colors = listOf(accentColor, Color(0xFFFFB300)),
                        cornerRadius = 12.dp
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xDD080E1A))
            .border(
                width = if (isClaimable) 1.4.dp else 1.dp,
                color = when {
                    isClaimable -> accentColor
                    isClaimed -> Color(0xFF141F2E)
                    else -> Color(0xFF1B2A3D)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        FloatingRewardAnchor(
            rewardText = "+${directive.rewardStars} ★  +${directive.rewardPoints} PTS",
            triggerKey = claimToastKey,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-10).dp)
        )

        // Specular Sheen Pass for Claimable Directives
        if (isClaimable) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val sheenX = w * sheenProgress
                val sheenWidth = w * 0.35f

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(sheenX, 0f),
                        end = Offset(sheenX + sheenWidth, h)
                    ),
                    size = Size(w, h)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Difficulty Badge + Dual Rewards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.18f))
                        .border(0.5.dp, accentColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = directive.difficulty.label,
                        color = accentColor,
                        fontSize = 8.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "+${directive.rewardPoints} PTS",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "+${directive.rewardStars} ★",
                            color = Color(0xFFFFB300),
                            fontSize = 10.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = directive.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = directive.description,
                    color = Color(0xFF8FA3BF),
                    fontSize = 10.sp,
                    fontFamily = ChakraPetchFontFamily,
                    lineHeight = 13.sp
                )
            }

            // Progress & Tactical Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented Progress Gauge
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isClaimed) "DIRECTIVE FULFILLED" else "PROGRESS TELEMETRY",
                            color = Color(0xFF556980),
                            fontSize = 8.sp,
                            fontFamily = ChakraPetchFontFamily
                        )
                        Text(
                            text = "${directive.currentProgress} / ${directive.targetProgress}",
                            color = accentColor,
                            fontSize = 9.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Normalizes visual bars: if target <= 8, use target. Otherwise lock to 10 bold tactical blocks.
                        val totalSegments = if (directive.targetProgress <= 8) directive.targetProgress.coerceAtLeast(1) else 10
                        val gap = 2.5.dp.toPx()
                        val segW = (w - (gap * (totalSegments - 1))) / totalSegments

                        val progressRatio = (directive.currentProgress.toFloat() / directive.targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val activeSegments = (progressRatio * totalSegments).toInt()

                        for (i in 0 until totalSegments) {
                            val x = i * (segW + gap)
                            val isActive = i < activeSegments

                            drawRoundRect(
                                color = if (isActive) accentColor else Color(0xFF121B2B),
                                topLeft = Offset(x, 0f),
                                size = Size(segW, h),
                                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                            )
                        }
                    }
                }

                // CTA Action Button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .then(
                            if (isClaimable) {
                                Modifier.background(Brush.horizontalGradient(listOf(accentColor, Color(0xFFFFB300))))
                            } else {
                                Modifier.background(if (isClaimed) Color(0xFF101724) else Color(0xFF131F30))
                            }
                        )
                        .border(
                            0.8.dp,
                            if (isClaimable) Color.White.copy(alpha = 0.5f) else Color(0xFF1C2D44),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            enabled = !isClaimed,
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (isClaimable) {
                                claimToastKey = System.currentTimeMillis()
                                onClaim()
                            } else {
                                onDeploy()
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isClaimed -> "VERIFIED"
                            isClaimable -> "▶ CLAIM"
                            else -> "▶ DEPLOY"
                        },
                        color = when {
                            isClaimed -> Color(0xFF556980)
                            isClaimable -> Color(0xFF040711)
                            else -> Color(0xFF00E5FF)
                        },
                        fontSize = 10.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
