package com.example.gridsurge.ui.modifiers

import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.cyberBorderGlow(
    colors: List<Color> = listOf(Color(0xFF00E5FF), Color(0xFFFF0055), Color.Transparent),
    strokeWidth: Dp = 2.dp,
    cornerRadius: Dp = 12.dp
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "borderGlow")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    drawWithContent {
        drawContent()
        
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            )
        }

        val movingGradient = Brush.linearGradient(
            colors = colors,
            start = Offset(size.width * phase, 0f),
            end = Offset(size.width * (phase + 0.5f), size.height)
        )

        drawPath(
            path = path,
            brush = movingGradient,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}
