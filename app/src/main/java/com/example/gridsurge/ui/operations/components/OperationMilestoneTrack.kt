package com.example.gridsurge.ui.operations.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.operations.model.MilestoneCacheState
import com.example.gridsurge.operations.model.MilestoneTier
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import kotlin.math.sin

@Composable
fun OperationMilestoneTrack(
    currentPoints: Int,
    maxPoints: Int,
    milestones: List<MilestoneCacheState>,
    onClaimMilestone: (MilestoneTier) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "conduitPulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xDD070B14))
            .border(1.dp, Color(0xFF1B283A), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header: Point Gauge Telemetry
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                    Text(
                        text = "OPERATION MILESTONE CONDUIT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currentPoints",
                        color = Color(0xFF00E5FF),
                        fontSize = 15.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = " / $maxPoints PTS",
                        color = Color(0xFF8FA3BF),
                        fontSize = 10.sp,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Milestone Conduit Track with Interactive Nodes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Conduit Pipe & Flowing Plasma Line
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.Center)
                ) {
                    val w = size.width
                    val h = size.height
                    val progressRatio = (currentPoints.toFloat() / maxPoints).coerceIn(0f, 1f)
                    val activeWidth = w * progressRatio

                    // Outer Metallic Conduit Slot
                    drawRoundRect(
                        color = Color(0xFF101926),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(h / 2f, h / 2f)
                    )

                    // Sub-conduit Groove Line
                    drawRoundRect(
                        color = Color(0xFF0A0F17),
                        topLeft = Offset(2f, 2f),
                        size = Size(w - 4f, h - 4f),
                        cornerRadius = CornerRadius((h - 4f) / 2f, (h - 4f) / 2f)
                    )

                    if (activeWidth > 0f) {
                        // High-Voltage Plasma Core
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0088FF), Color(0xFF00E5FF), Color(0xFF00FF66)),
                                startX = 0f,
                                endX = activeWidth
                            ),
                            size = Size(activeWidth, h),
                            cornerRadius = CornerRadius(h / 2f, h / 2f)
                        )

                        // Travelling Pulse Wave
                        val waveX = (activeWidth * ((sin(pulsePhase) + 1f) / 2f)).coerceIn(0f, activeWidth)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color.Transparent),
                                center = Offset(waveX, h / 2f),
                                radius = 18f
                            ),
                            radius = 18f,
                            center = Offset(waveX, h / 2f)
                        )
                    }
                }

                // Interactive Milestone Crate Nodes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    milestones.forEach { milestone ->
                        val isReached = currentPoints >= milestone.tier.targetPoints
                        MilestoneNode(
                            milestone = milestone,
                            isReached = isReached,
                            onClick = {
                                if (isReached && !milestone.isClaimed) {
                                    onClaimMilestone(milestone.tier)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneNode(
    milestone: MilestoneCacheState,
    isReached: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isReached && !milestone.isClaimed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nodeScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "readyGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val nodeColor = when {
        milestone.isClaimed -> Color(0xFF00FF66)
        isReached -> Color(0xFFFFB300)
        else -> Color(0xFF556980)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Crate Box
        Box(
            modifier = Modifier
                .size(42.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .then(
                    if (isReached && !milestone.isClaimed) {
                        Modifier.cyberBorderGlow(
                            colors = listOf(Color(0xFFFFB300), Color(0xFFFF1744)),
                            cornerRadius = 8.dp
                        )
                    } else Modifier
                )
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        milestone.isClaimed -> Color(0xFF0E1A16)
                        isReached -> Color(0xFF241C0A)
                        else -> Color(0xFF090F1A)
                    }
                )
                .border(
                    width = if (isReached && !milestone.isClaimed) 1.5.dp else 1.dp,
                    color = if (isReached && !milestone.isClaimed) Color(0xFFFFB300).copy(alpha = pulseGlow) else nodeColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(
                    enabled = isReached && !milestone.isClaimed,
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                milestone.isClaimed -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Claimed",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(18.dp)
                    )
                }
                isReached -> {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Ready",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(20.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF435368),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Target Label
        Text(
            text = "${milestone.tier.targetPoints} PTS",
            color = if (isReached) Color.White else Color(0xFF556980),
            fontSize = 9.sp,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}
