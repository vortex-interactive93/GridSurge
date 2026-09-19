package com.example.gridsurge.ui.clash.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun BlitzClashActiveVisor(
    playerScore: Long,
    rivalScore: Long,
    secondsRemaining: Int,
    playerCallsign: String,
    rivalCallsign: String,
    modifier: Modifier = Modifier
) {
    val cyanTheme = Color(0xFF00E5FF)
    val crimsonTheme = Color(0xFFFF0055)

    // 1. Odometer Score Animations
    val animPlayerScore by animateIntAsState(
        targetValue = playerScore.toInt(),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "playerOdometer"
    )

    val animRivalScore by animateIntAsState(
        targetValue = rivalScore.toInt(),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "rivalOdometer"
    )

    // 2. Spring Physics Tug-o-War Bias Calculation
    val totalScore = max(playerScore + rivalScore, 1000L).toFloat()
    val targetBias = (playerScore.toFloat() / totalScore).coerceIn(0.10f, 0.90f)

    val animatedBias by animateFloatAsState(
        targetValue = if (playerScore == 0L && rivalScore == 0L) 0.5f else targetBias,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nodeSpring"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --- TOP ROW: OPERATOR BANNERS & CLOCK ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Mini-Card
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = playerCallsign.uppercase(),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format(Locale.US, "%,d", animPlayerScore),
                    color = cyanTheme,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Central Match Clock
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xB30A0E17))
                    .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                val mm = (secondsRemaining / 60).toString().padStart(2, '0')
                val ss = (secondsRemaining % 60).toString().padStart(2, '0')
                Text(
                    text = "$mm:$ss",
                    color = if (secondsRemaining <= 10) crimsonTheme else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Rival Mini-Card
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = rivalCallsign.uppercase(),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format(Locale.US, "%,d", animRivalScore),
                    color = crimsonTheme,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- BOTTOM ROW: OVERCHARGE TUG-O-WAR BAR ---
        val delta = playerScore - rivalScore
        val deficitText = when {
            delta > 0 -> String.format(Locale.US, "+%,d LEAD", delta)
            delta < 0 -> String.format(Locale.US, "-%,d DEFICIT", abs(delta))
            else -> "TIED // 0"
        }
        val deficitColor = when {
            delta > 0 -> cyanTheme
            delta < 0 -> crimsonTheme
            else -> Color.Gray
        }

        // Floating Lead/Deficit Indicator above the Node
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = deficitText,
                color = deficitColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Glowing Physics Bar
        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            val barY = size.height / 2f
            val nodeX = size.width * animatedBias

            // Left (Player) Beam
            drawLine(
                color = cyanTheme,
                start = Offset(0f, barY),
                end = Offset(nodeX, barY),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )

            // Right (Rival) Beam
            drawLine(
                color = crimsonTheme,
                start = Offset(nodeX, barY),
                end = Offset(size.width, barY),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )

            // Center Clash Node (Glowing Diamond)
            val path = Path().apply {
                moveTo(nodeX, barY - 18f)
                lineTo(nodeX + 18f, barY)
                lineTo(nodeX, barY + 18f)
                lineTo(nodeX - 18f, barY)
                close()
            }

            // Outer Glow
            drawPath(path, color = if (delta >= 0) cyanTheme.copy(alpha = 0.5f) else crimsonTheme.copy(alpha = 0.5f))
            // Inner Core
            drawPath(path, color = Color.White, style = Fill)
        }
    }
}
