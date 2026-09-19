package com.example.gridsurge.ui.glitch.components

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.glitch.model.GlitchVisorTelemetry
import com.example.gridsurge.game.glitch.model.PurityThreatLevel
import com.example.gridsurge.game.glitch.model.SpectrumAnalyzerState
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun TacticalGlitchVisor(
    telemetry: GlitchVisorTelemetry,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val purityClamped = telemetry.systemPurity.coerceIn(0f, 1f)
    val isCritical = purityClamped <= 0.30f || telemetry.isCritical

    val infiniteTransition = rememberInfiniteTransition(label = "visorLoop")

    val alertPulse by if (isCritical) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(220, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alertGlow"
        )
    } else {
        remember { mutableFloatStateOf(0.85f) }
    }

    // High-speed oscilloscope phase ticker
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f * 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "oscilloscopePhase"
    )

    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenPass"
    )

    val threatColor = when {
        purityClamped > 0.60f -> Color(0xFF00FF66) // Nominal Emerald
        purityClamped > 0.30f -> Color(0xFFFFD600) // Caution Amber
        else -> Color(0xFFFF0055)                           // Critical Crimson
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ==================== 1. UPPER HARDWARE TELEMETRY CONSOLE ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xF0050A14))
                .border(1.2.dp, threatColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Top Row: Recessed Diegetic Data Tiles + Pause Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RecessedDataModule(
                        label = "SCORE",
                        value = String.format(Locale.US, "%,d", telemetry.score),
                        accentColor = Color.White,
                        modifier = Modifier.weight(1.1f)
                    )

                    RecessedDataModule(
                        label = "TIME",
                        value = telemetry.formattedTime,
                        accentColor = Color(0xFF00FF66),
                        modifier = Modifier.weight(0.9f)
                    )

                    RecessedDataModule(
                        label = "PURGED",
                        value = "${telemetry.catalystsPurged}/${telemetry.targetPurgeQuota}",
                        accentColor = threatColor,
                        modifier = Modifier.weight(1.0f)
                    )

                    // Chamfered Hexagonal Pause Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0C1626))
                            .border(1.dp, Color(0xFF1F3354), RoundedCornerShape(6.dp))
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

                // ==================== 2. SYSTEM PURITY REACTOR DISPLAY ====================
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(threatColor.copy(alpha = alertPulse))
                            )
                            Text(
                                text = when {
                                    purityClamped <= 0.30f -> "CONTAINMENT FAILURE"
                                    purityClamped <= 0.60f -> "VOLTAGE LEAKAGE"
                                    else -> "SYSTEM INTEGRITY // NOMINAL"
                                },
                                color = threatColor.copy(alpha = alertPulse),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // FIXED: Dynamically reflects true purity float
                        Text(
                            text = "${(purityClamped * 100).toInt()}%",
                            color = if (isCritical) threatColor else Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // 24-Segment LED Array
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                    ) {
                        val totalSegments = 24
                        val gap = 2.dp.toPx()
                        val segWidth = (size.width - (gap * (totalSegments - 1))) / totalSegments
                        val litCount = (purityClamped * totalSegments).toInt()

                        for (i in 0 until totalSegments) {
                            val x = i * (segWidth + gap)
                            val isLit = i < litCount

                            drawRoundRect(
                                color = if (isLit) threatColor.copy(alpha = alertPulse) else Color(0xFF0A111C),
                                topLeft = Offset(x, 0f),
                                size = Size(segWidth, size.height),
                                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                            )

                            if (!isLit) {
                                drawRoundRect(
                                    color = Color(0xFF142033),
                                    topLeft = Offset(x, 0f),
                                    size = Size(segWidth, size.height),
                                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                                    style = Stroke(0.8.dp.toPx())
                                )
                            }
                        }

                        // Specular Light Sweep
                        val sheenX = size.width * sheenProgress
                        val sheenW = size.width * 0.25f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.20f), Color.Transparent),
                                start = Offset(sheenX, 0f),
                                end = Offset(sheenX + sheenW, size.height)
                            ),
                            size = size
                        )
                    }
                }
            }
        }

        // ==================== 3. ANOMALY SPECTRUM BRIDGE (ACCELERATED JITTER) ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xBB040710))
                .border(0.8.dp, Color(0xFF142236), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEED 2026-09-10 • WAVE 0${telemetry.activeWave}",
                    color = Color(0xFF556980),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // High-Speed Live Audio-Reactive Spectrum Wave
                Canvas(
                    modifier = Modifier
                        .width(130.dp)
                        .height(22.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f
                    val points = 28
                    val step = w / (points - 1)

                    val path = Path()
                    val threatFactor = (1.0f - purityClamped).coerceIn(0f, 1f)

                    for (i in 0 until points) {
                        val x = i * step
                        val normX = i.toFloat() / points
                        val baseWave = sin(normX * 12f + wavePhase) * (h * 0.28f)
                        val harmonic = sin(normX * 24f - wavePhase * 1.5f) * (h * 0.15f)

                        // Noise jitter spikes on low purity
                        val jitter = if (isCritical) (Random.nextFloat() - 0.5f) * h * 0.45f else 0f
                        val y = centerY + (baseWave + harmonic + jitter) * (0.4f + threatFactor * 0.6f)

                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    // Ambient glow stroke
                    drawPath(
                        path = path,
                        color = threatColor.copy(alpha = 0.4f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // High-contrast core stroke
                    drawPath(
                        path = path,
                        color = threatColor,
                        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "ANOMALY SEED // EQUAL",
                    color = Color(0xFF00E5FF),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecessedDataModule(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC09101D))
            .border(0.8.dp, Color(0xFF17263C), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                color = Color(0xFF556980),
                fontSize = 7.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = accentColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }
    }
}
