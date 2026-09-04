package com.example.gridsurge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridsurge.audio.HapticType
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.features.adventure.model.NeuralAugment
import com.example.gridsurge.features.adventure.model.ObjectiveType
import com.example.gridsurge.features.adventure.model.RelicCyberWareState
import com.example.gridsurge.features.adventure.ui.components.ActiveRelicButton
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.CyberChamferShape
import kotlinx.coroutines.delay
import java.util.Locale

enum class VisorGameMode {
    CLASSIC,
    ADVENTURE,
    TIME_BLITZ,
    DAILY_GLITCH,
    BLITZ_CLASH
}

@Composable
fun AdaptiveCyberVisorHeader(
    gameMode: VisorGameMode,
    score: Long,
    rivalScore: Long = 0L,
    highScore: Long = 0L,
    linesCleared: Int = 0,
    elapsedSeconds: Int = 0,
    timeRemainingSec: Float = 0f,
    activeCores: Int = 0,
    totalCores: Int = 4,
    catalystsPurged: Int = 0,
    totalCatalysts: Int = 20,
    comboStreak: Int = 0,
    feverProgress: Float = 0f,
    isFeverActive: Boolean = false,
    objectiveType: ObjectiveType = ObjectiveType.INFECTED_PURGE,
    movesRemaining: Int = 0,
    resonanceEnergy: Float = 0f,
    isWarpReady: Boolean = false,
    boardOccupancy: Float = 0f,
    activeAugments: List<NeuralAugment> = emptyList(),
    relicState: RelicCyberWareState? = null,
    onRelicActivate: () -> Unit = {},
    onRelicDragStart: (Float, Float) -> Unit = { _, _ -> },
    onRelicDrag: (Float, Float) -> Unit = { _, _ -> },
    onRelicDragEnd: () -> Unit = {},
    onRelicDragCancel: () -> Unit = {},
    onPauseClick: () -> Unit,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    // 1. Determine Critical Time State (Time Blitz only)
    val isCriticalTime = (gameMode == VisorGameMode.TIME_BLITZ && timeRemainingSec in 0.1f..15.0f) ||
                         (gameMode == VisorGameMode.ADVENTURE && movesRemaining in 1..3)

    // 2. Audio & Haptics Loop for Crisis State
    LaunchedEffect(isCriticalTime) {
        if (isCriticalTime) {
            while (true) {
                SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 0.55f)
                SfxManager.triggerHaptic(HapticType.DOUBLE_CRACK)
                delay(650L)
            }
        }
    }

    // 3. Pulse Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnim")
    val pulseAlpha by if (isCriticalTime) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val pulseScale by if (isCriticalTime) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    // 4. Format Display Times
    val blitzSec = timeRemainingSec.toInt().coerceAtLeast(0)
    val blitzMm = (blitzSec / 60).toString().padStart(2, '0')
    val blitzSs = (blitzSec % 60).toString().padStart(2, '0')

    val timerMm = (elapsedSeconds / 60).toString().padStart(2, '0')
    val timerSs = (elapsedSeconds % 60).toString().padStart(2, '0')

    // 5. Container & Chamfer Plate
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(68.dp)
    ) {
        // Base Obsidian Glass
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xF00A1424), Color(0xDD040812))
                    ),
                    shape = CyberChamferShape
                )
                .border(
                    width = 1.dp,
                    color = if (isCriticalTime) Color(0x66FF0055) else Color(0x6600E5FF),
                    shape = CyberChamferShape
                )
        )

        // Metallic Brackets Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cut = 12.dp.toPx()
            val armLen = 14.dp.toPx()
            val bracketThickness = 2.dp.toPx()
            val bracketColor = if (isCriticalTime) Color(0xFFFF0055) else Color(0xFF00E5FF)
            val subBracketColor = Color(0xFF263859)

            // Top-Left
            val tlPath = Path().apply {
                moveTo(0f, cut + armLen)
                lineTo(0f, cut)
                lineTo(cut, 0f)
                lineTo(cut + armLen, 0f)
            }
            drawPath(tlPath, bracketColor, style = Stroke(width = bracketThickness, cap = StrokeCap.Square, join = StrokeJoin.Miter))

            // Top-Right
            val trPath = Path().apply {
                moveTo(w - cut - armLen, 0f)
                lineTo(w - cut, 0f)
                lineTo(w, cut)
                lineTo(w, cut + armLen)
            }
            drawPath(trPath, bracketColor, style = Stroke(width = bracketThickness, cap = StrokeCap.Square, join = StrokeJoin.Miter))

            // Bottom-Left
            val blPath = Path().apply {
                moveTo(0f, h - cut - armLen)
                lineTo(0f, h - cut)
                lineTo(cut, h)
                lineTo(cut + armLen, h)
            }
            drawPath(blPath, bracketColor, style = Stroke(width = bracketThickness, cap = StrokeCap.Square, join = StrokeJoin.Miter))

            // Bottom-Right
            val brPath = Path().apply {
                moveTo(w - cut - armLen, h)
                lineTo(w - cut, h)
                lineTo(w, h - cut)
                lineTo(w, h - cut - armLen)
            }
            drawPath(brPath, bracketColor, style = Stroke(width = bracketThickness, cap = StrokeCap.Square, join = StrokeJoin.Miter))

            // Center Notch Trim
            val midX = w / 2f
            drawLine(subBracketColor, Offset(midX - 24.dp.toPx(), 0f), Offset(midX + 24.dp.toPx(), 0f), 2.dp.toPx())
            drawLine(subBracketColor, Offset(midX - 16.dp.toPx(), h), Offset(midX + 16.dp.toPx(), h), 2.dp.toPx())
        }

        // Main Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1: Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SLOT 1: SCORE / YOU
                TelemetrySlot(
                    label = if (gameMode == VisorGameMode.BLITZ_CLASH) "YOU" else "SCORE",
                    value = String.format(Locale.US, "%,d", score),
                    valueColor = if (gameMode == VisorGameMode.BLITZ_CLASH) Color(0xFF00E5FF) else Color.White,
                    alignment = Alignment.Start
                )

                // SLOT 2: MODE-SPECIFIC METRIC (TIME)
                val (timeLabel, timeValue, timeColor) = when (gameMode) {
                    VisorGameMode.TIME_BLITZ -> Triple(
                        if (isCriticalTime) "CRITICAL" else "TIME",
                        "$blitzMm:$blitzSs",
                        if (isCriticalTime) Color(0xFFFF0055).copy(alpha = pulseAlpha) else Color(0xFF00FF66)
                    )
                    else -> Triple("TIME", "$timerMm:$timerSs", Color(0xFF00FF66))
                }

                TelemetrySlot(
                    label = timeLabel,
                    value = timeValue,
                    valueColor = timeColor,
                    labelColor = if (isCriticalTime) Color(0xFFFF0055) else Color(0xFF5C8599),
                    alignment = Alignment.CenterHorizontally,
                    modifier = if (isCriticalTime) Modifier.scale(pulseScale) else Modifier
                )

                // SLOT 3: MODE-SPECIFIC OBJECTIVE (LINES/CORES/FEVER/RIVAL)
                val (objLabel, objValue, objColor) = when (gameMode) {
                    VisorGameMode.BLITZ_CLASH -> Triple("RIVAL", String.format(Locale.US, "%,d", rivalScore), Color(0xFFFF0055))
                    VisorGameMode.CLASSIC -> {
                        val isComboActive = comboStreak > 1
                        Triple(
                            if (isComboActive) "COMBO" else "LINES",
                            if (isComboActive) "x$comboStreak" else "$linesCleared",
                            if (isComboActive) Color(0xFFFF9900) else Color(0xFF00E5FF)
                        )
                    }
                    VisorGameMode.ADVENTURE -> {
                        val label = when (objectiveType) {
                            ObjectiveType.LINE_CLEANSE -> "LINES"
                            ObjectiveType.CHROMA_SYNTHESIS -> "TILES"
                            ObjectiveType.SURGE_STREAK_TARGET -> "STREAK"
                            else -> "CORES"
                        }
                        val color = when (objectiveType) {
                            ObjectiveType.CHROMA_SYNTHESIS -> Color(0xFF00FF66)
                            ObjectiveType.SURGE_STREAK_TARGET -> Color(0xFFFF9900)
                            ObjectiveType.MOVE_BUDGET_SWEEP -> if (isCriticalTime) Color(0xFFFF0055) else Color(0xFF00FF66)
                            else -> Color(0xFF00E5FF)
                        }
                        val value = if (objectiveType == ObjectiveType.MOVE_BUDGET_SWEEP) {
                            "$movesRemaining"
                        } else if (objectiveType == ObjectiveType.SURGE_STREAK_TARGET) {
                            "$comboStreak/$totalCores"
                        } else {
                            "$activeCores/$totalCores"
                        }
                        Triple(label, value, color)
                    }
                    VisorGameMode.TIME_BLITZ -> {
                        val feverPercent = (feverProgress * 100).toInt().coerceIn(0, 100)
                        Triple(
                            "FEVER",
                            if (isFeverActive) "MAX" else "$feverPercent%",
                            if (isFeverActive) Color(0xFFFF0055) else Color(0xFF00E5FF)
                        )
                    }
                    VisorGameMode.DAILY_GLITCH -> Triple("PURGED", "$catalystsPurged/$totalCatalysts", Color(0xFF00FF66))
                    else -> Triple("", "", Color.White)
                }

                    TelemetrySlot(
                        label = objLabel,
                        value = objValue,
                        valueColor = objColor,
                        alignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onPauseClick() }
                    )

                // SLOT 4: TACTICAL PAUSE BUTTON
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x2B00E5FF))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                        .clickable(enabled = isEnabled) {
                            SfxManager.playSfx(SfxType.MODAL_WHOOSH)
                            onPauseClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color.White, RoundedCornerShape(1.dp)))
                        Box(modifier = Modifier.width(3.dp).height(16.dp).background(Color.White, RoundedCornerShape(1.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun PhaseResonanceGauge(
    energy: Float,
    isWarpReady: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (energy / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "resonanceFill"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "warpReady")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "PHASE RESONANCE",
                color = Color(0xFF5C8599),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isWarpReady) "WARP CORE SYNTHESIZED" else "${energy.toInt()}%",
                color = if (isWarpReady) Color(0xFFEA80FC) else Color(0xFF00E5FF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF101622))
                .border(
                    width = 0.5.dp,
                    color = if (isWarpReady) Color(0xFFEA80FC).copy(alpha = glowAlpha) else Color(0xFF1C2C4A),
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            // Fill Layer
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        brush = Brush.horizontalGradient(
                            if (isWarpReady) {
                                listOf(Color(0xFFEA80FC), Color(0xFFD500F9))
                            } else {
                                listOf(Color(0xFF00B8D4), Color(0xFF00E5FF))
                            }
                        )
                    )
            )
            
            // Pulsing Glow for Warp Ready
            if (isWarpReady) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEA80FC).copy(alpha = glowAlpha * 0.3f))
                )
            }
        }
    }
}



@Composable
private fun ConsolidatedAdventureProgressRow(
    resonanceEnergy: Float,
    isWarpReady: Boolean,
    objectiveProgress: Float,
    objectiveText: String,
    relicState: RelicCyberWareState? = null,
    onRelicActivate: () -> Unit = {},
    onRelicDragStart: (Float, Float) -> Unit = { _, _ -> },
    onRelicDrag: (Float, Float) -> Unit = { _, _ -> },
    onRelicDragEnd: () -> Unit = {},
    onRelicDragCancel: () -> Unit = {},
    isEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .clip(CyberChamferShape)
            .background(Color(0xCC060C18))
            .border(1.dp, Color(0xFF1B2A42), CyberChamferShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Stage Directive Progress
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DIRECTIVE PROGRESS",
                    color = Color(0xFF5C8599),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = objectiveText,
                    color = if (objectiveProgress >= 1f) Color(0xFF00FF66) else Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }

            // Dual-Color Directive Progress Bar
            val animatedObjProgress by animateFloatAsState(
                targetValue = objectiveProgress.coerceIn(0f, 1f),
                animationSpec = tween(500),
                label = "objProgress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF08101E))
                    .border(0.5.dp, Color(0xFF1A2A44), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedObjProgress)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF00FF66))
                            )
                        )
                )
            }
        }

        // RIGHT: Active Relic Skill Module (if unlocked)
        if (relicState != null && relicState.isUnlocked) {
            Spacer(modifier = Modifier.width(10.dp))

            ActiveRelicButton(
                relicState = relicState,
                onActivate = onRelicActivate,
                onRelicDragStart = onRelicDragStart,
                onRelicDrag = onRelicDrag,
                onRelicDragEnd = onRelicDragEnd,
                onRelicDragCancel = onRelicDragCancel,
                isEnabled = isEnabled,
                compact = true
            )
        }
    }
}

@Composable
private fun TelemetrySlot(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color = Color(0xFF5C8599),
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = alignment,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 17.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
    }
}
