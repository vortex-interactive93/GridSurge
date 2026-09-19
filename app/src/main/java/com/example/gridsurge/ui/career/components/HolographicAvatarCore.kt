package com.example.gridsurge.ui.career.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicAvatarCore(
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarHolo")

    val outerRingAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRing"
    )

    val innerRingAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRing"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = (w / 2f) * 0.88f

            // 1. Solid Substrate Base
            drawCircle(
                color = Color(0xFF060B14),
                radius = r,
                center = Offset(cx, cy)
            )

            // 2. Ambient Core Bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = 0.45f * pulse), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r * 0.9f
                ),
                radius = r * 0.9f,
                center = Offset(cx, cy)
            )

            // 3. Outer Segmented Reticle Ring
            rotate(outerRingAngle, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.35f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f, 6f, 14f))
                    )
                )
            }

            // 4. Inner Counter-Rotating Hex Lattice Frame
            rotate(innerRingAngle, pivot = Offset(cx, cy)) {
                val hexRadius = r * 0.62f
                val hexPath = Path()
                for (i in 0..5) {
                    val angle = Math.toRadians(60.0 * i - 30.0)
                    val x = (cx + hexRadius * cos(angle)).toFloat()
                    val y = (cy + hexRadius * sin(angle)).toFloat()
                    if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                }
                hexPath.close()

                drawPath(
                    path = hexPath,
                    color = accentColor.copy(alpha = 0.85f * pulse),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // 5. Central Tactical Iris Glyph
            drawCircle(
                color = Color.White.copy(alpha = 0.95f * pulse),
                radius = 3.5.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Crosshair tick marks
            val tickLen = 6.dp.toPx()
            drawLine(
                color = accentColor,
                start = Offset(cx, cy - r * 0.4f),
                end = Offset(cx, cy - r * 0.4f - tickLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = accentColor,
                start = Offset(cx, cy + r * 0.4f),
                end = Offset(cx, cy + r * 0.4f + tickLen),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
