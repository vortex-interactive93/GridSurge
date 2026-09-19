package com.example.gridsurge.ui.clash.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

@Composable
fun OverchargeMomentumBar(
    playerScore: Long,
    rivalScore: Long,
    modifier: Modifier = Modifier
) {
    // 1. Dynamic Sensitivity: 4,000 pts reaches 100% deflection
    val maxLeadThreshold = 4000f
    val scoreDelta = (playerScore - rivalScore).toFloat()
    val targetNormalizedLead = (scoreDelta / maxLeadThreshold).coerceIn(-1f, 1f)

    // Snappy spring-physics deflection
    val animatedLead by animateFloatAsState(
        targetValue = targetNormalizedLead,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "momentumSpring"
    )

    // Pulsing energy wave
    val infiniteTransition = rememberInfiniteTransition(label = "plasmaPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val playerColor = Color(0xFF00E5FF) // Neon Cyan
    val rivalColor = Color(0xFFFF0055)  // Crimson Anomaly
    val isPlayerLeading = scoreDelta >= 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP STATUS BADGE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Score Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(playerColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.US, "%,d", playerScore),
                    color = playerColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Center Lead/Deficit Metric Tag
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xCC0B101D),
                border = BorderStroke(
                    1.dp,
                    if (isPlayerLeading) playerColor.copy(alpha = 0.5f) else rivalColor.copy(alpha = 0.5f)
                )
            ) {
                val leadText = when {
                    scoreDelta > 0 -> String.format(Locale.US, "+%,d LEAD", scoreDelta.toLong())
                    scoreDelta < 0 -> String.format(Locale.US, "-%,d DEFICIT", abs(scoreDelta).toLong())
                    else -> "TIED // 0"
                }
                Text(
                    text = leadText,
                    color = if (isPlayerLeading) playerColor else rivalColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Rival Score Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.US, "%,d", rivalScore),
                    color = rivalColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(rivalColor, RoundedCornerShape(2.dp))
                )
            }
        }

        // KINETIC TUG-OF-WAR CHASSIS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val centerZeroX = w / 2f
                val maxTravel = (w / 2f) - 24.dp.toPx()
                val nodeX = centerZeroX + (animatedLead * maxTravel)

                // 1. Outer Dark Chassis with Chamfered Ends
                val chassisPath = Path().apply {
                    val chamfer = 10.dp.toPx()
                    moveTo(chamfer, 0f)
                    lineTo(w - chamfer, 0f)
                    lineTo(w, chamfer)
                    lineTo(w, h - chamfer)
                    lineTo(w - chamfer, h)
                    lineTo(chamfer, h)
                    lineTo(0f, h - chamfer)
                    lineTo(0f, chamfer)
                    close()
                }

                // Chassis Matte Fill
                drawPath(
                    path = chassisPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF141923), Color(0xFF090D15))
                    )
                )
                // Chassis Border Stroke
                drawPath(
                    path = chassisPath,
                    color = Color(0x3300E5FF),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // 2. Recessed Track Trench
                val trackY = h / 2f
                val trackHeight = 10.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF04060A),
                    topLeft = Offset(8.dp.toPx(), trackY - trackHeight / 2f),
                    size = Size(w - 16.dp.toPx(), trackHeight),
                    cornerRadius = CornerRadius(5.dp.toPx())
                )

                // 3. Player Energy Plasma (Left to Node)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(playerColor.copy(alpha = 0.3f), playerColor),
                        startX = 8.dp.toPx(),
                        endX = nodeX
                    ),
                    start = Offset(8.dp.toPx(), trackY),
                    end = Offset(nodeX, trackY),
                    strokeWidth = 6.dp.toPx()
                )
                // White-hot core beam
                drawLine(
                    color = Color.White.copy(alpha = pulseAlpha),
                    start = Offset(8.dp.toPx(), trackY),
                    end = Offset(nodeX, trackY),
                    strokeWidth = 1.8.dp.toPx()
                )

                // 4. Rival Energy Plasma (Node to Right)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(rivalColor, rivalColor.copy(alpha = 0.3f)),
                        startX = nodeX,
                        endX = w - 8.dp.toPx()
                    ),
                    start = Offset(nodeX, trackY),
                    end = Offset(w - 8.dp.toPx(), trackY),
                    strokeWidth = 6.dp.toPx()
                )
                // White-hot core beam
                drawLine(
                    color = Color.White.copy(alpha = pulseAlpha),
                    start = Offset(nodeX, trackY),
                    end = Offset(w - 8.dp.toPx(), trackY),
                    strokeWidth = 1.8.dp.toPx()
                )

                // 5. Impact Collision Flash Aura (Where beams collide)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            if (isPlayerLeading) playerColor else rivalColor,
                            Color.Transparent
                        ),
                        center = Offset(nodeX, trackY),
                        radius = 26.dp.toPx()
                    ),
                    center = Offset(nodeX, trackY),
                    radius = 26.dp.toPx()
                )

                // 6. Faceted 3D Diamond Impact Node
                val nodeR = 14.dp.toPx()
                val activeGlow = if (isPlayerLeading) playerColor else rivalColor

                // Top Facet (Light Reflection)
                val topFacet = Path().apply {
                    moveTo(nodeX, trackY - nodeR)
                    lineTo(nodeX + nodeR, trackY)
                    lineTo(nodeX, trackY)
                    close()
                }
                drawPath(topFacet, color = Color(0xFFD1D5DB), style = Fill)

                // Left Facet (Medium Metal)
                val leftFacet = Path().apply {
                    moveTo(nodeX, trackY - nodeR)
                    lineTo(nodeX - nodeR, trackY)
                    lineTo(nodeX, trackY)
                    close()
                }
                drawPath(leftFacet, color = Color(0xFF6B7280), style = Fill)

                // Bottom Left Facet (Dark Gunmetal Shadow)
                val blFacet = Path().apply {
                    moveTo(nodeX - nodeR, trackY)
                    lineTo(nodeX, trackY + nodeR)
                    lineTo(nodeX, trackY)
                    close()
                }
                drawPath(blFacet, color = Color(0xFF374151), style = Fill)

                // Bottom Right Facet (Deep Shadow)
                val brFacet = Path().apply {
                    moveTo(nodeX + nodeR, trackY)
                    lineTo(nodeX, trackY + nodeR)
                    lineTo(nodeX, trackY)
                    close()
                }
                drawPath(brFacet, color = Color(0xFF1F2937), style = Fill)

                // Diamond Edge Seams (Chrome Rim)
                val diamondOutline = Path().apply {
                    moveTo(nodeX, trackY - nodeR)
                    lineTo(nodeX + nodeR, trackY)
                    lineTo(nodeX, trackY + nodeR)
                    lineTo(nodeX - nodeR, trackY)
                    close()
                }
                drawPath(diamondOutline, color = activeGlow, style = Stroke(width = 1.8.dp.toPx()))
                
                // Vertical Center Core Seam
                drawLine(
                    color = Color.White,
                    start = Offset(nodeX, trackY - (nodeR * 0.7f)),
                    end = Offset(nodeX, trackY + (nodeR * 0.7f)),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}
