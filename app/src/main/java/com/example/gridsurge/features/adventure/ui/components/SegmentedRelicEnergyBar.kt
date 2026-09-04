package com.example.gridsurge.features.adventure.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.HapticType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.features.adventure.model.RelicCyberWareState
import com.example.gridsurge.ui.CyberChamferShape

/**
 * High-Tech Segmented Sci-Fi Relic Energy Bar.
 * Renders a 16-pip vector battery meter with custom gradient glow and touch/drag controls.
 */
@Composable
fun SegmentedRelicEnergyBar(
    relicState: RelicCyberWareState,
    onActivate: () -> Unit,
    onRelicDragStart: (Float, Float) -> Unit = { _, _ -> },
    onRelicDrag: (Float, Float) -> Unit = { _, _ -> },
    onRelicDragEnd: () -> Unit = {},
    onRelicDragCancel: () -> Unit = {},
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val ability = relicState.abilityType
    val accentColor = Color(ability.colorHex)
    val isReady = relicState.isReady && isEnabled && relicState.isUnlocked
    val chargePercent = (relicState.chargeProgress * 100).toInt().coerceIn(0, 100)

    val infiniteTransition = rememberInfiniteTransition(label = "pipGlow")
    val glowAlpha by if (isReady) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val pulseScale by if (isReady) {
        infiniteTransition.animateFloat(
            initialValue = 0.995f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    var barPosInWindow by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .clip(CyberChamferShape)
            .background(Color(0xF0060C18))
            .border(
                width = 1.dp,
                color = when {
                    isReady -> accentColor.copy(alpha = glowAlpha)
                    relicState.isOverclockDanger -> Color(0xFFFF0055).copy(alpha = glowAlpha)
                    else -> Color(0xFF1B2A42)
                },
                shape = CyberChamferShape
            )
            .onGloballyPositioned { coords -> barPosInWindow = coords.positionInWindow() }
            .pointerInput(isReady) {
                if (!isReady) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        SfxManager.playSfx(SfxType.OVERDRIVE_ACTIVATE)
                        SfxManager.triggerHaptic(HapticType.DOUBLE_CRACK)
                        onRelicDragStart(barPosInWindow.x + offset.x, barPosInWindow.y + offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onRelicDrag(barPosInWindow.x + change.position.x, barPosInWindow.y + change.position.y)
                    },
                    onDragEnd = onRelicDragEnd,
                    onDragCancel = onRelicDragCancel
                )
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ABILITY: ${ability.title}",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                val statusText = when {
                    isReady -> "100% - READY"
                    relicState.isOverclockDanger -> "$chargePercent% - DANGER (2X)"
                    else -> "$chargePercent% - CHARGING"
                }

                val statusColor = when {
                    isReady -> accentColor.copy(alpha = glowAlpha)
                    relicState.isOverclockDanger -> Color(0xFFFF0055).copy(alpha = glowAlpha)
                    else -> Color(0xFFFFD600)
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // 16 Vector-Drawn Segmented Battery Pips
            val pipsCount = 16
            val filledPipsCount = ((relicState.chargeProgress) * pipsCount).toInt().coerceIn(0, pipsCount)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                val w = size.width
                val h = size.height
                val pipSpacing = 3.dp.toPx()
                val totalSpacing = (pipsCount - 1) * pipSpacing
                val pipWidth = (w - totalSpacing) / pipsCount.toFloat()
                val corner = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                for (i in 0 until pipsCount) {
                    val pipLeft = i * (pipWidth + pipSpacing)
                    val isPipFilled = i < filledPipsCount

                    if (isPipFilled) {
                        // Active Glowing Pip
                        val startColor = if (isReady) accentColor else Color(0xFFFFD600)
                        val endColor = if (isReady) Color.White else Color(0xFFFF9900)
                        
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(startColor, endColor)
                            ),
                            topLeft = Offset(pipLeft, 0f),
                            size = Size(pipWidth, h),
                            cornerRadius = corner
                        )

                        // Outer Glow Border when ready
                        if (isReady) {
                            drawRoundRect(
                                color = accentColor.copy(alpha = glowAlpha * 0.8f),
                                topLeft = Offset(pipLeft - 0.5f, -0.5f),
                                size = Size(pipWidth + 1f, h + 1f),
                                cornerRadius = corner,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    } else {
                        // Empty Track Pip
                        drawRoundRect(
                            color = Color(0x33101E2E),
                            topLeft = Offset(pipLeft, 0f),
                            size = Size(pipWidth, h),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            color = Color(0x221B2A42),
                            topLeft = Offset(pipLeft, 0f),
                            size = Size(pipWidth, h),
                            cornerRadius = corner,
                            style = Stroke(width = 0.8.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
