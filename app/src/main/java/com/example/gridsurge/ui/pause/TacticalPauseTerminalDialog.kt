package com.example.gridsurge.ui.pause

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.pause.model.PauseConfirmationIntent
import com.example.gridsurge.ui.pause.model.PauseMissionTelemetry
import com.example.gridsurge.ui.pause.model.PauseTerminalConfig
import com.example.gridsurge.ui.pause.model.TelemetryPillSpec
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily
import java.util.Locale

val CyberBezelShape = GenericShape { size, _ ->
    val cut = 14f
    moveTo(cut, 0f)
    lineTo(size.width - cut, 0f)
    lineTo(size.width, cut)
    lineTo(size.width, size.height - cut)
    lineTo(size.width - cut, size.height)
    lineTo(cut, size.height)
    lineTo(0f, size.height - cut)
    lineTo(0f, cut)
    close()
}

@Composable
fun TacticalPauseTerminalDialog(
    telemetry: PauseMissionTelemetry,
    onResume: () -> Unit,
    onOpenSettings: () -> Unit,
    onRestartMatch: () -> Unit,
    onAbortToHub: () -> Unit,
    isRestartAvailable: Boolean = true,
    abortActionLabel: String = "ABORT RUN",
    modifier: Modifier = Modifier
) {
    val config = remember(telemetry, isRestartAvailable) {
        val parts = telemetry.timeElapsedFormatted.split(":")
        val mm = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val ss = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val totalSec = mm * 60 + ss

        PauseTerminalConfig(
            modeTitle = telemetry.gameModeTitle,
            modeSubtitle = telemetry.sectorOrSeedTag,
            directiveText = telemetry.directive,
            elapsedSeconds = totalSec,
            telemetryMetrics = listOf(
                TelemetryPillSpec("SCORE", String.format(Locale.US, "%,d", telemetry.currentScore), Color.White),
                TelemetryPillSpec(telemetry.primaryMetricLabel, telemetry.primaryMetricValue, telemetry.primaryMetricColor),
                TelemetryPillSpec(telemetry.secondaryMetricLabel, telemetry.secondaryMetricValue, Color(0xFF00E5FF))
            ),
            isRestartPermitted = isRestartAvailable && telemetry.isRestartAvailable,
            activeConfirmation = PauseConfirmationIntent.NONE
        )
    }

    TacticalPauseTerminalDialog(
        config = config,
        onResume = onResume,
        onOpenSettings = onOpenSettings,
        onRestartMatch = onRestartMatch,
        onAbortToHub = onAbortToHub,
        abortActionLabel = abortActionLabel,
        modifier = modifier
    )
}

@Composable
fun TacticalPauseTerminalDialog(
    config: PauseTerminalConfig,
    onResume: () -> Unit,
    onOpenSettings: () -> Unit,
    onRestartMatch: () -> Unit,
    onAbortToHub: () -> Unit,
    abortActionLabel: String = "ABORT RUN",
    modifier: Modifier = Modifier
) {
    var confirmationIntent by remember { mutableStateOf(config.activeConfirmation) }

    val infiniteTransition = rememberInfiniteTransition(label = "pauseTerminalLoop")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    Dialog(
        onDismissRequest = onResume,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC03060B))
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .cyberBorderGlow(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFF004466)),
                        cornerRadius = 14.dp
                    )
                    .clip(CyberBezelShape)
                    .background(Color(0xF5060C16))
                    .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.85f), CyberBezelShape)
            ) {
                // Vector Canvas Graticules & Corner Brackets
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val cut = 14f

                    val bracketColor = Color(0xFF00E5FF)
                    val bracketLen = 20.dp.toPx()
                    val strokeW = 2.dp.toPx()

                    // Top-Left Tech Flange
                    drawLine(bracketColor, Offset(0f, cut + bracketLen), Offset(0f, cut), strokeW)
                    drawLine(bracketColor, Offset(0f, cut), Offset(cut, 0f), strokeW)
                    drawLine(bracketColor, Offset(cut, 0f), Offset(cut + bracketLen, 0f), strokeW)

                    // Bottom-Right Tech Flange
                    drawLine(bracketColor, Offset(w, h - cut - bracketLen), Offset(w, h - cut), strokeW)
                    drawLine(bracketColor, Offset(w, h - cut), Offset(w - cut, h), strokeW)
                    drawLine(bracketColor, Offset(w - cut, h), Offset(w - cut - bracketLen, h), strokeW)

                    // Subtle Technical Horizontal Lines
                    for (i in 1..4) {
                        val y = (h / 5f) * i
                        drawLine(
                            color = Color(0xFF00E5FF).copy(alpha = 0.03f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ==================== 1. STATUS BAR HEADER ====================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = "STASIS INTERRUPT",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }

                        Text(
                            text = String.format(
                                Locale.US,
                                "%02d:%02d",
                                config.elapsedSeconds / 60,
                                config.elapsedSeconds % 60
                            ),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "SYSTEM PAUSED",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp
                    )

                    // ==================== 2. MISSION INTEL & TELEMETRY ====================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x990A1322))
                            .border(1.dp, Color(0xFF16253C), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = config.modeTitle,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = config.modeSubtitle,
                                    color = Color(0xFF556980),
                                    fontSize = 8.sp,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Text(
                                text = config.directiveText,
                                color = Color(0xFF8FA3BF),
                                fontSize = 10.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp
                            )

                            // Telemetry Metrics Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                config.telemetryMetrics.forEach { metric ->
                                    ConsoleTelemetryPill(
                                        metric = metric,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // ==================== 3. ACTION CONTROLS ====================
                    if (confirmationIntent == PauseConfirmationIntent.NONE) {
                        // PRIMARY: Resume Protocol Button
                        TacticalActionButton(
                            text = "RESUME PROTOCOL",
                            icon = Icons.Default.PlayArrow,
                            isPrimary = true,
                            sheenProgress = sheenProgress,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onResume()
                            }
                        )

                        // SECONDARY: System Settings Button
                        TacticalActionButton(
                            text = "SYSTEM SETTINGS",
                            icon = Icons.Default.Settings,
                            isPrimary = false,
                            onClick = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onOpenSettings()
                            }
                        )

                        // TERTIARY: Destructive Session Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (config.isRestartPermitted) {
                                DestructiveTextButton(
                                    text = "RESTART MATCH",
                                    color = Color(0xFFFFD600),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        SfxManager.playSfx(SfxType.SNAP_TICK)
                                        confirmationIntent = PauseConfirmationIntent.CONFIRM_RESTART
                                    }
                                )
                            }

                            DestructiveTextButton(
                                text = abortActionLabel,
                                color = Color(0xFFFF0055),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    SfxManager.playSfx(SfxType.SNAP_TICK)
                                    confirmationIntent = PauseConfirmationIntent.CONFIRM_ABORT
                                }
                            )
                        }
                    } else {
                        // Inline Destructive Verification Guard
                        DestructiveVerificationDock(
                            isRestart = confirmationIntent == PauseConfirmationIntent.CONFIRM_RESTART,
                            abortActionLabel = abortActionLabel,
                            onConfirm = {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                if (confirmationIntent == PauseConfirmationIntent.CONFIRM_RESTART) {
                                    onRestartMatch()
                                } else {
                                    onAbortToHub()
                                }
                                confirmationIntent = PauseConfirmationIntent.NONE
                            },
                            onCancel = {
                                SfxManager.playSfx(SfxType.UI_BACK)
                                confirmationIntent = PauseConfirmationIntent.NONE
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==================== TACTICAL SUB-COMPONENTS ====================

@Composable
private fun ConsoleTelemetryPill(
    metric: TelemetryPillSpec,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC070E1A))
            .border(0.8.dp, Color(0xFF142236), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(metric.accentColor)
                )
                Text(
                    text = metric.label,
                    color = Color(0xFF556980),
                    fontSize = 7.5.sp,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = metric.value,
                color = metric.accentColor,
                fontSize = 13.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun TacticalActionButton(
    text: String,
    icon: ImageVector,
    isPrimary: Boolean,
    sheenProgress: Float = 0f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    val baseBackground = if (isPrimary) {
        Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF007799)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF101B2E), Color(0xFF0A1220)))
    }

    val contentColor = if (isPrimary) Color(0xFF03070E) else Color(0xFFC2D4EC)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CyberBezelShape)
            .background(baseBackground)
            .border(
                1.dp,
                if (isPrimary) Color(0xFF00E5FF) else Color(0xFF1E3554),
                CyberBezelShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isPrimary) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val sheenX = w * sheenProgress
                val sheenWidth = w * 0.35f

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.30f), Color.Transparent),
                        start = Offset(sheenX, 0f),
                        end = Offset(sheenX + sheenWidth, h)
                    ),
                    size = Size(w, h)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.5.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
private fun DestructiveTextButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.2.dp, color.copy(alpha = 0.60f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.5.sp,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun DestructiveVerificationDock(
    isRestart: Boolean,
    abortActionLabel: String = "ABORT RUN",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val themeColor = if (isRestart) Color(0xFFFFD600) else Color(0xFFFF0055)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(themeColor.copy(alpha = 0.12f))
            .border(1.dp, themeColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isRestart) "CONFIRM RESTART?" else "CONFIRM $abortActionLabel?",
                    color = themeColor,
                    fontSize = 10.5.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF131D2E))
                        .border(0.8.dp, Color(0xFF22354F), RoundedCornerShape(4.dp))
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CANCEL",
                        color = Color(0xFF8FA3BF),
                        fontSize = 9.5.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(themeColor)
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONFIRM",
                        color = Color(0xFF03070E),
                        fontSize = 9.5.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
