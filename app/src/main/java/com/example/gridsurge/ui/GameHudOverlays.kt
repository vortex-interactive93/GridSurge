package com.example.gridsurge.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.R
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.audio.VoxAction
import com.example.gridsurge.settings.SettingsManager
import com.example.gridsurge.ui.settings.SettingsDialog
import androidx.compose.ui.platform.LocalContext

private val CyberDark = Color(0xFF12141C)
private val NeonCyan = Color(0xFF00E5FF)

private val NeonCrimson = Color(0xFFFF0055)
private val CardBorder = Color(0xFF1B2A4A)

// 45-Degree Chamfered Corner Cyber Polygon Shape
val CyberChamferShape = GenericShape { size, _ ->
    val cut = 16f
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
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onResume,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CyberDark,
            border = BorderStroke(2.dp, NeonCyan)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "PAUSED",
                    color = NeonCyan,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                CyberButton("RESUME", NeonCyan, {
                    SfxManager.playSfx(SfxType.BUTTON_CLICK)
                    onResume()
                })
                Spacer(modifier = Modifier.height(16.dp))
                CyberButton("SETTINGS", Color.White, {
                    SfxManager.playSfx(SfxType.BUTTON_CLICK)
                    showSettingsDialog = true
                })
                Spacer(modifier = Modifier.height(16.dp))
                CyberButton("RESTART", Color.White, {
                    SfxManager.playSfx(SfxType.BUTTON_CLICK)
                    onRestart()
                })
                Spacer(modifier = Modifier.height(16.dp))
                CyberButton("QUIT", Color.Red, {
                    SfxManager.playSfx(SfxType.BUTTON_CLICK)
                    onQuit()
                })
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                settingsManager = SettingsManager.getInstance(context),
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

@Composable
fun GameOverDialog(
    score: Long,
    bestScore: Long,
    isNewRecord: Boolean,
    onRestart: () -> Unit,
    onExitToHub: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC03060C)),
            contentAlignment = Alignment.Center
        ) {
            // Main Vector Cyber Card Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .wrapContentHeight()
            ) {
                // Vector Canvas: Cyber Borders, Glowing Conduits & Scanlines
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val cut = 24.dp.toPx()

                    val framePath = Path().apply {
                        moveTo(cut, 0f)
                        lineTo(w - cut, 0f)
                        lineTo(w, cut)
                        lineTo(w, h - cut)
                        lineTo(w - cut, h)
                        lineTo(cut, h)
                        lineTo(0f, h - cut)
                        lineTo(0f, cut)
                        close()
                    }

                    // 1. Dark Backdrop Fill
                    drawPath(
                        path = framePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F1524), Color(0xFF070B12))
                        )
                    )

                    // 2. Glowing Vector Stroke
                    drawPath(
                        path = framePath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonCrimson.copy(alpha = glowAlpha),
                                CardBorder,
                                NeonCyan.copy(alpha = glowAlpha * 0.7f)
                            )
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 3. Tech Corner Accents
                    val cornerLen = 18.dp.toPx()

                    // Top-Left Accent
                    drawLine(NeonCrimson, Offset(0f, cut + cornerLen), Offset(0f, cut), 3.dp.toPx())
                    drawLine(NeonCrimson, Offset(0f, cut), Offset(cut, 0f), 3.dp.toPx())
                    drawLine(NeonCrimson, Offset(cut, 0f), Offset(cut + cornerLen, 0f), 3.dp.toPx())

                    // Bottom-Right Accent
                    drawLine(NeonCyan, Offset(w, h - cut - cornerLen), Offset(w, h - cut), 3.dp.toPx())
                    drawLine(NeonCyan, Offset(w, h - cut), Offset(w - cut, h), 3.dp.toPx())
                    drawLine(NeonCyan, Offset(w - cut, h), Offset(w - cut - cornerLen, h), 3.dp.toPx())
                }

                // Foreground Content Layout (Strict Column Hierarchy)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Status
                    Text(
                        text = "CRITICAL FAILURE",
                        color = NeonCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "MATRIX LOCKED",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dedicated New Record Banner (Placed Above Scores, Never Overlapping)
                    if (isNewRecord) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0x3300E5FF), Color.Transparent)
                                    )
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "★ NEW ALL-TIME RECORD ★",
                                color = NeonCyan.copy(alpha = glowAlpha),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Score Readout Container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x5503060C), shape = CyberChamferShape)
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FINAL SCORE",
                            color = Color(0xFF6B7D99),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = String.format("%,d", score),
                            color = NeonCyan,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PERSONAL BEST: ",
                                color = Color(0xFF8A99AD),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format("%,d", bestScore),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Primary Action: Reboot Matrix
                    CyberActionButton(
                        text = "REBOOT MATRIX",
                        primaryColor = NeonCyan,
                        isPrimary = true,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onRestart()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Action: Abort to Hub
                    CyberActionButton(
                        text = "ABORT TO HUB",
                        primaryColor = Color(0xFF8A99AD),
                        isPrimary = false,
                        onClick = {
                            SfxManager.playSfx(SfxType.UI_CONFIRM)
                            onExitToHub()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CyberActionButton(
    text: String,
    primaryColor: Color,
    isPrimary: Boolean,
    outlineBrush: Brush? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cut = 12.dp.toPx()
            val w = size.width
            val h = size.height

            val btnPath = Path().apply {
                moveTo(cut, 0f)
                lineTo(w - cut, 0f)
                lineTo(w, cut)
                lineTo(w, h - cut)
                lineTo(w - cut, h)
                lineTo(cut, h)
                lineTo(0f, h - cut)
                lineTo(0f, cut)
                close()
            }

            if (isPrimary) {
                drawPath(
                    path = btnPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF007A8C), Color(0xFF00B4D8))
                    )
                )
                drawPath(
                    path = btnPath,
                    color = primaryColor,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            } else {
                drawPath(
                    path = btnPath,
                    color = Color(0x33101624)
                )
                if (outlineBrush != null) {
                    drawPath(
                        path = btnPath,
                        brush = outlineBrush,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else {
                    drawPath(
                        path = btnPath,
                        color = Color(0xFF26354D),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isPrimary) Color.Black else primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CyberButton(text: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(text, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}
