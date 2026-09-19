package com.example.gridsurge.ui.campaign.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.gridsurge.audio.SfxManager
import com.example.gridsurge.audio.SfxType
import com.example.gridsurge.game.campaign.model.CampaignStageNode
import com.example.gridsurge.game.campaign.model.StageNodeStatus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SectorNodeGraphCanvas(
    nodes: List<CampaignStageNode>,
    onNodeSelected: (CampaignStageNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapFx")

    // Photon conduit propagation phase
    val photonPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "photons"
    )

    // Current stage sonar radar pulse
    val sonarProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "sonar"
    )

    // Current stage reticle rotation angle
    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(nodes) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val clickRadiusSq = (34.dp.toPx()) * (34.dp.toPx())

                    // Hit test nodes in reverse order (top of visual stack first)
                    for (node in nodes.reversed()) {
                        val nx = node.xNorm * w
                        val ny = node.yNorm * h
                        val distSq = (tapOffset.x - nx) * (tapOffset.x - nx) + (tapOffset.y - ny) * (tapOffset.y - ny)

                        if (distSq <= clickRadiusSq) {
                            if (node.status != StageNodeStatus.LOCKED) {
                                SfxManager.playSfx(SfxType.SNAP_TICK, overridePitch = 1.3f)
                                onNodeSelected(node)
                            } else {
                                SfxManager.playSfx(SfxType.INVALID_MOVE, overridePitch = 0.7f)
                            }
                            return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // ==================== 1. CONDUIT TRACE PASS ====================
        for (i in 0 until nodes.size - 1) {
            val from = nodes[i]
            val to = nodes[i + 1]

            val p0 = Offset(from.xNorm * w, from.yNorm * h)
            val p1 = Offset(to.xNorm * w, to.yNorm * h)

            val isTraceUnlocked = to.status != StageNodeStatus.LOCKED
            val traceBaseColor = if (isTraceUnlocked) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFF142033)

            // Base physical circuit trace
            drawLine(
                color = traceBaseColor,
                start = p0,
                end = p1,
                strokeWidth = if (isTraceUnlocked) 3.dp.toPx() else 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Animated High-Voltage Photon Packets (Active Conduits Only)
            if (isTraceUnlocked) {
                val packetCount = 3
                for (k in 0 until packetCount) {
                    val t = (photonPhase + (k.toFloat() / packetCount)) % 1f
                    val curX = p0.x + (p1.x - p0.x) * t
                    val curY = p0.y + (p1.y - p0.y) * t

                    // Photon Glow Core
                    drawCircle(
                        color = Color(0xFF00FFFF),
                        radius = 3.dp.toPx(),
                        center = Offset(curX, curY)
                    )
                }
            }
        }

        // ==================== 2. HARDWARE NODE PASS ====================
        nodes.forEach { node ->
            val center = Offset(node.xNorm * w, node.yNorm * h)
            val nodeRadius = 24.dp.toPx()

            when (node.status) {
                StageNodeStatus.CURRENT_ACTIVE -> {
                    // --- SONAR RADAR PING ---
                    val maxSonarRadius = nodeRadius * 2.2f
                    val currentSonarR = nodeRadius + (maxSonarRadius - nodeRadius) * sonarProgress
                    val sonarAlpha = (1f - sonarProgress).coerceIn(0f, 1f)

                    drawCircle(
                        color = Color(0xFF00FF66).copy(alpha = sonarAlpha * 0.45f),
                        radius = currentSonarR,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // --- ROTATING COMBAT RETICLE BRACKETS ---
                    val rad = Math.toRadians(reticleRotation.toDouble())
                    val bracketDist = nodeRadius * 1.35f
                    for (angleStep in 0..3) {
                        val baseAngle = rad + (angleStep * (Math.PI / 2.0))
                        val bx = center.x + (cos(baseAngle) * bracketDist).toFloat()
                        val by = center.y + (sin(baseAngle) * bracketDist).toFloat()
                        drawCircle(Color(0xFF00FF66), radius = 2.dp.toPx(), center = Offset(bx, by))
                    }

                    // Active Core Node Chassis
                    drawCircle(Color(0xFF040A14), radius = nodeRadius, center = center)
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFF00FF66), Color(0xFF003816)), center, nodeRadius),
                        radius = nodeRadius * 0.85f,
                        center = center
                    )
                    drawCircle(Color(0xFF00FF66), radius = nodeRadius, center = center, style = Stroke(2.dp.toPx()))
                }

                StageNodeStatus.MASTERED_THREE_STAR,
                StageNodeStatus.CLEARED_TWO_STAR,
                StageNodeStatus.CLEARED_ONE_STAR -> {
                    // Cleared/Mastered Node: High-Tech Solid Alloy
                    drawCircle(Color(0xFF060D1A), radius = nodeRadius, center = center)
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.4f), Color(0xFF061426)), center, nodeRadius),
                        radius = nodeRadius * 0.85f,
                        center = center
                    )
                    drawCircle(Color(0xFF00E5FF), radius = nodeRadius, center = center, style = Stroke(1.5.dp.toPx()))

                    // Star Pips cluster beneath node
                    drawStarPipsCluster(center, node.starsEarned, nodeRadius)
                }

                StageNodeStatus.LOCKED -> {
                    // Locked Node: Dimmed Substrate
                    drawCircle(Color(0xFF070B12), radius = nodeRadius * 0.8f, center = center)
                    drawCircle(Color(0xFF162233), radius = nodeRadius * 0.8f, center = center, style = Stroke(1.dp.toPx()))
                }
            }
        }
    }
}

private fun DrawScope.drawStarPipsCluster(center: Offset, starCount: Int, nodeRadius: Float) {
    val pipY = center.y + nodeRadius + 6.dp.toPx()
    val pipSpacing = 8.dp.toPx()
    val startX = center.x - ((3 - 1) * pipSpacing) / 2f

    for (i in 0 until 3) {
        val px = startX + (i * pipSpacing)
        val isLit = i < starCount
        drawCircle(
            color = if (isLit) Color(0xFFFFD600) else Color(0xFF1E2E44),
            radius = 2.dp.toPx(),
            center = Offset(px, pipY)
        )
    }
}
