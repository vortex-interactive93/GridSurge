package com.example.gridsurge.ui.campaign.dialogs

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.campaign.model.CampaignStageNode
import com.example.gridsurge.ui.CyberChamferShape
import com.example.gridsurge.ui.modifiers.cyberBorderGlow
import com.example.gridsurge.ui.theme.ChakraPetchFontFamily
import com.example.gridsurge.ui.theme.OrbitronFontFamily

@Composable
fun TacticalStageBriefingDialog(
    stage: CampaignStageNode,
    onDeploy: () -> Unit,
    onDismiss: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "deployScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "briefingSheen")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "sheen"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE602050A))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .cyberBorderGlow(listOf(Color(0xFF00E5FF), Color(0xFF003855)), 16.dp)
                    .clip(CyberChamferShape)
                    .background(Color(0xFF070D18))
                    .border(1.5.dp, Color(0xFF00E5FF), CyberChamferShape)
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header: Stage Code & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(Color(0xFF00E5FF)))
                            Text(
                                text = "TACTICAL RECON // ${stage.stageCode}",
                                color = Color(0xFF00E5FF),
                                fontSize = 9.sp,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F1B2C))
                                .clickable {
                                    SfxManager.playSfx(SfxType.UI_BACK)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8FA3BF), modifier = Modifier.size(14.dp))
                        }
                    }

                    Text(
                        text = stage.stageTitle,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )

                    // ==================== 1. HOLOGRAPHIC GRID BLUEPRINT PREVIEW ====================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC091220))
                            .border(1.dp, Color(0xFF172840), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Micro Wireframe Grid Canvas
                        Canvas(modifier = Modifier.size(66.dp)) {
                            val cells = 8
                            val cellSize = size.width / cells
                            for (r in 0 until cells) {
                                for (c in 0 until cells) {
                                    val isGlitch = stage.blueprint.initialGlitchCoords.contains(Pair(c, r))
                                    val isObstacle = stage.blueprint.prefilledBlockCoords.contains(Pair(c, r))

                                    val cellColor = when {
                                        isGlitch -> Color(0xFFFF0055)
                                        isObstacle -> Color(0xFF8A2BE2)
                                        else -> Color(0xFF0D1C2E)
                                    }

                                    drawRoundRect(
                                        color = cellColor,
                                        topLeft = Offset(c * cellSize + 1f, r * cellSize + 1f),
                                        size = Size(cellSize - 2f, cellSize - 2f),
                                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Hazard Intel Description
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(stage.hazardType.badgeColor.copy(alpha = 0.2f))
                                    .border(0.6.dp, stage.hazardType.badgeColor, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stage.hazardType.label,
                                    color = stage.hazardType.badgeColor,
                                    fontSize = 7.5.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "High-threat matrix anomaly. Clear surrounding perimeter lines to neutralize catalysts before meltdown.",
                                color = Color(0xFF8FA3BF),
                                fontSize = 8.5.sp,
                                fontFamily = ChakraPetchFontFamily,
                                lineHeight = 11.sp
                            )
                        }
                    }

                    // ==================== 2. STAR DIRECTIVE CARDS ====================
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        stage.objectives.forEach { objective ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (objective.isCompleted) Color(0x3300FF66) else Color(0xFF0A1322))
                                    .border(0.8.dp, if (objective.isCompleted) Color(0xFF00FF66) else Color(0xFF1B2B42), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (objective.isCompleted) Color(0xFFFFD600) else Color(0xFF4A5C74),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = objective.description,
                                        color = if (objective.isCompleted) Color.White else Color(0xFF8FA3BF),
                                        fontSize = 9.sp,
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = if (objective.isCompleted) "ACHIEVED" else "+1 ★",
                                    color = if (objective.isCompleted) Color(0xFF00FF66) else Color(0xFF556980),
                                    fontSize = 8.sp,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ==================== 3. ENGAGE / DEPLOY BUTTON ====================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF00FF66), Color(0xFF008833)))
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                SfxManager.playSfx(SfxType.UI_CONFIRM)
                                onDeploy()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val w = size.width
                            val h = size.height
                            val sheenX = w * sheenProgress
                            val sheenW = w * 0.35f
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.32f), Color.Transparent),
                                    start = Offset(sheenX, 0f),
                                    end = Offset(sheenX + sheenW, h)
                                ),
                                size = Size(w, h)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF03070E), modifier = Modifier.size(18.dp))
                            Text(
                                text = "DEPLOY TO SECTOR // ENGAGE",
                                color = Color(0xFF03070E),
                                fontSize = 11.5.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
